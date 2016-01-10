#import "opusenc.h"

#include "opus.h"
#include "opus_multistream.h"

#include "ogg.h"

#include "opus_header.h"

static bool comment_init(char **comments, int* length, const char *vendor_string);
static bool comment_add(char **comments, int* length, char *tag, char *val);
static bool comment_pad(char **comments, int* length, int amount);

static inline int writeOggPage(ogg_page *page, NSFileHandle *fileHandle)
{
    int written = page->header_len + page->body_len;
    
    [fileHandle writeData:[[NSData alloc] initWithBytesNoCopy:page->header length:page->header_len freeWhenDone:false]];
    [fileHandle writeData:[[NSData alloc] initWithBytesNoCopy:page->body length:page->body_len freeWhenDone:false]];
    
    return MAX(0, written);
}

@interface TGOggOpusWriter ()
{
    NSFileHandle *_fileHandle;
    
    OpusEncoder *_encoder;
    uint8_t *_packet;
    
    oe_enc_opt inopt;
    
    ogg_stream_state os;
    ogg_page og;
    ogg_packet op;
    ogg_int64_t last_granulepos;
    ogg_int64_t enc_granulepos;
    int last_segments;
    int eos;
    OpusHeader header;
    
    ogg_int32_t _packetId;
    int size_segments;
    
    opus_int64 nb_encoded;
    opus_int64 bytes_written;
    opus_int64 pages_out;
    opus_int64 total_bytes;
    opus_int64 total_samples;
    opus_int32 nb_samples;
    opus_int32 peak_bytes;
    opus_int32 min_bytes;
    
    int max_frame_bytes;
    opus_int32 bitrate;
    opus_int32 rate;
    opus_int32 coding_rate;
    opus_int32 frame_size;
    int with_cvbr;
    int max_ogg_delay;
    int comment_padding;
    int serialno;
    opus_int32 lookahead;
}

@end

@implementation TGOggOpusWriter

- (instancetype)init
{
    self = [super init];
    if (self != nil)
    {
        bitrate = 20 * 1024;
        rate = 48000;
        coding_rate = 16000;
        frame_size = 960;
        with_cvbr = 1;
        max_ogg_delay = 48000;
        comment_padding = 512;
        
        _packetId = -1;
    }
    return self;
}

- (void)cleanup
{
    if (_encoder != NULL)
    {
        opus_encoder_destroy(_encoder);
        _encoder = NULL;
    }
    
    ogg_stream_clear(&os);
    
    if (_packet != NULL)
    {
        free(_packet);
        _packet = NULL;
    }
}

- (bool)begin:(NSFileHandle *)fileHandle
{
    _fileHandle = fileHandle;
    
    inopt.channels = 1;
    inopt.rate = coding_rate=rate;
    inopt.gain = 0;
    inopt.samplesize = 16;
    inopt.endianness = 0;
    inopt.rawmode = 0;
    inopt.ignorelength = 0;
    inopt.copy_comments = 0;
    
    arc4random_buf(&serialno, sizeof(serialno));
    
    const char *opus_version = opus_get_version_string();
    comment_init(&inopt.comments, &inopt.comments_length, opus_version);
    
    bitrate = 16 * 1024;
    
    inopt.rawmode = 1;
    inopt.ignorelength = 1;
    inopt.samplesize = 16;
    inopt.rate = 16000;
    inopt.channels = 1;
    
    rate = inopt.rate;
    inopt.skip = 0;
    
    // In order to code the complete length we'll need to do a little padding
    //setup_padder(&inopt, &original_samples);
    
    if (rate > 24000)
        coding_rate = 48000;
    else if (rate > 16000)
        coding_rate = 24000;
    else if (rate > 12000)
        coding_rate = 16000;
    else if (rate > 8000)
        coding_rate = 12000;
    else
        coding_rate = 8000;
    
    // Scale the resampler complexity, but only for 48000 output because the near-cutoff behavior matters a lot more at lower rates
    if (rate != coding_rate)
    {
        NSLog(@"Invalid rate");
        return false;
    }
    
    header.channels = 1;
    header.channel_mapping = 0;
    header.input_sample_rate = rate;
    header.gain = inopt.gain;
    header.nb_streams = 1;
    
    int result = OPUS_OK;
    _encoder = opus_encoder_create(coding_rate, 1, OPUS_APPLICATION_AUDIO, &result);
    if (result != OPUS_OK)
    {
        NSLog(@"Error cannot create encoder: %s", opus_strerror(result));
        return false;
    }
    
    min_bytes = max_frame_bytes = (1275 * 3 + 7) * header.nb_streams;
    _packet = malloc(max_frame_bytes);
    
    result = opus_encoder_ctl(_encoder, OPUS_SET_BITRATE(bitrate));
    if (result != OPUS_OK)
    {
        NSLog(@"Error OPUS_SET_BITRATE returned: %s", opus_strerror(result));
        return false;
    }
    
    /*result = opus_encoder_ctl(_encoder, OPUS_SET_VBR(1));
    if (result != OPUS_OK)
    {
        NSLog(@"Error OPUS_SET_VBR returned: %s", opus_strerror(result));
        return false;
    }*/
    
    /*ret = opus_multistream_encoder_ctl(st, OPUS_SET_VBR_CONSTRAINT(1));
    if (ret != OPUS_OK)
    {
        NSLog(@"Error OPUS_SET_VBR_CONSTRAINT returned: %s", opus_strerror(ret));
        return false;
    }*/
    
    /*ret = opus_multistream_encoder_ctl(st, OPUS_SET_COMPLEXITY(complexity));
    if(ret != OPUS_OK)
    {
        NSLog(@"Error OPUS_SET_COMPLEXITY returned: %s", opus_strerror(ret));
        return false;
    }*/
    
    /*result = opus_encoder_ctl(st, OPUS_SET_PACKET_LOSS_PERC(expect_loss));
    if (ret != OPUS_OK)
    {
        NSLog(@"Error OPUS_SET_PACKET_LOSS_PERC returned: %s", opus_strerror(ret));
        return false;
    }*/
    
#ifdef OPUS_SET_LSB_DEPTH
    result = opus_encoder_ctl(_encoder, OPUS_SET_LSB_DEPTH(MAX(8, MIN(24, inopt.samplesize))));
    if (result != OPUS_OK)
    {
        NSLog(@"Warning OPUS_SET_LSB_DEPTH returned: %s", opus_strerror(result));
    }
#endif
    
    // We do the lookahead check late so user CTLs can change it
    result = opus_encoder_ctl(_encoder, OPUS_GET_LOOKAHEAD(&lookahead));
    if (result != OPUS_OK)
    {
        NSLog(@"Error OPUS_GET_LOOKAHEAD returned: %s", opus_strerror(result));
        return false;
    }
    
    inopt.skip += lookahead;
    // Regardless of the rate we're coding at the ogg timestamping/skip is always timed at 48000.
    header.preskip = (int)(inopt.skip * (48000.0 / coding_rate));
    // Extra samples that need to be read to compensate for the pre-skip
    inopt.extraout = (int)(header.preskip * (rate / 48000.0));
    
    // Initialize Ogg stream struct
    if (ogg_stream_init(&os, serialno) == -1)
    {
        NSLog(@"Error: stream init failed");
        return false;
    }
    
    // Write header
    {
        unsigned char header_data[100];
        int packet_size = opus_header_to_packet(&header, header_data, 100);
        op.packet = header_data;
        op.bytes = packet_size;
        op.b_o_s = 1;
        op.e_o_s = 0;
        op.granulepos = 0;
        op.packetno = 0;
        ogg_stream_packetin(&os, &op);
        
        while ((result = ogg_stream_flush(&os, &og)))
        {
            if (!result)
                break;
            
            int pageBytesWritten = writeOggPage(&og, _fileHandle);
            if (pageBytesWritten != og.header_len + og.body_len)
            {
                NSLog(@"Error: failed writing header to output stream");
                return false;
            }
            bytes_written += pageBytesWritten;
            pages_out++;
        }
        
        comment_pad(&inopt.comments, &inopt.comments_length, comment_padding);
        op.packet = (unsigned char *)inopt.comments;
        op.bytes = inopt.comments_length;
        op.b_o_s = 0;
        op.e_o_s = 0;
        op.granulepos = 0;
        op.packetno = 1;
        ogg_stream_packetin(&os, &op);
    }
    
    // Writing the rest of the opus header packets
    while ((result = ogg_stream_flush(&os, &og)))
    {
        if (result == 0)
            break;
        
        int writtenPageBytes = writeOggPage(&og, _fileHandle);
        if (writtenPageBytes != og.header_len + og.body_len)
        {
            NSLog(@"Error: failed writing header to output stream");
            return false;
        }
        
        bytes_written += writtenPageBytes;
        pages_out++;
    }
    
    free(inopt.comments);
    
    return true;
}

- (bool)writeFrame:(uint8_t *)framePcmBytes frameByteCount:(NSUInteger)frameByteCount
{
    // Main encoding loop (one frame per iteration)
    nb_samples = -1;

    int cur_frame_size = frame_size;
    _packetId++;
    
    if (nb_samples < 0)
    {
        nb_samples = frameByteCount / 2;
        total_samples += nb_samples;
        if (nb_samples < frame_size)
            op.e_o_s = 1;
        else
            op.e_o_s = 0;
    }
    op.e_o_s |= eos;
    
    int nbBytes = 0;
    
    if (nb_samples != 0)
    {
        uint8_t *paddedFrameBytes = framePcmBytes;
        bool freePaddedFrameBytes = false;
        
        if (nb_samples < cur_frame_size)
        {
            paddedFrameBytes = malloc(cur_frame_size * 2);
            freePaddedFrameBytes = true;
            
            memcpy(paddedFrameBytes, framePcmBytes, frameByteCount);
            memset(paddedFrameBytes + nb_samples * 2, 0, cur_frame_size * 2 - nb_samples * 2);
        }
        
        // Encode current frame
        nbBytes = opus_encode(_encoder, (opus_int16 *)paddedFrameBytes, cur_frame_size, _packet, max_frame_bytes / 10);
        if (freePaddedFrameBytes)
        {
            free(paddedFrameBytes);
            paddedFrameBytes = NULL;
        }
        
        if (nbBytes < 0)
        {
            NSLog(@"Encoding failed: %s. Aborting.", opus_strerror(nbBytes));
            return false;
        }
        
        nb_encoded += cur_frame_size;
        enc_granulepos += cur_frame_size * 48000 / coding_rate;
        total_bytes += nbBytes;
        size_segments = (nbBytes + 255) / 255;
        peak_bytes = MAX(nbBytes, peak_bytes);
        min_bytes = MIN(nbBytes, min_bytes);
    }
    
    // Flush early if adding this packet would make us end up with a continued page which we wouldn't have otherwise
    while ((((size_segments<=255)&&(last_segments+size_segments>255)) ||
            (enc_granulepos-last_granulepos>max_ogg_delay)) &&
           ogg_stream_flush_fill(&os, &og, 255 * 255))
    {
        if (ogg_page_packets(&og) != 0)
            last_granulepos = ogg_page_granulepos(&og);
        
        last_segments -= og.header[26];
        int writtenPageBytes = writeOggPage(&og, _fileHandle);
        if (writtenPageBytes != og.header_len + og.body_len)
        {
            NSLog(@"Error: failed writing data to output stream");
            return false;
        }
        bytes_written += writtenPageBytes;
        pages_out++;
    }
    
    op.packet = (unsigned char *)_packet;
    op.bytes = nbBytes;
    op.b_o_s = 0;
    op.granulepos = enc_granulepos;
    if (op.e_o_s)
    {
        /* We compute the final GP as ceil(len*48k/input_rate). When a resampling
         decoder does the matching floor(len*input/48k) conversion the length will
         be exactly the same as the input.
         */
        op.granulepos = ((total_samples * 48000 + rate - 1) / rate) + header.preskip;
    }
    op.packetno = 2 + _packetId;
    ogg_stream_packetin(&os, &op);
    last_segments += size_segments;
    
    /* The downside of early reading is if the input is an exact
     multiple of the frame_size you'll get an extra frame that needs
     to get cropped off. The downside of late reading is added delay.
     If your ogg_delay is 120ms or less we'll assume you want the
     low delay behavior.
     */
    /*if ((!op.e_o_s) && max_ogg_delay > 5760)
    {
        nb_samples = inopt.read_samples(inopt.readdata, input, frame_size);
        total_samples += nb_samples;
        if (nb_samples < frame_size)
            eos = 1;
        if (nb_samples == 0)
            op.e_o_s = 1;
    }
    else
        nb_samples = -1;*/
    
    // If the stream is over or we're sure that the delayed flush will fire, go ahead and flush now to avoid adding delay
    while ((op.e_o_s || (enc_granulepos + (frame_size * 48000 / coding_rate) - last_granulepos > max_ogg_delay) ||
            (last_segments >= 255)) ? ogg_stream_flush_fill(&os, &og, 255 * 255) : ogg_stream_pageout_fill(&os, &og, 255 * 255))
    {
        if (ogg_page_packets(&og) != 0)
            last_granulepos = ogg_page_granulepos(&og);
        last_segments -= og.header[26];
        int writtenPageBytes = writeOggPage(&og, _fileHandle);
        if (writtenPageBytes != og.header_len + og.body_len)
        {
            NSLog(@"Error: failed writing data to output stream");
            return false;
        }
        bytes_written += writtenPageBytes;
        pages_out++;
    }
    
    return true;
}

- (NSUInteger)encodedBytes
{
    return (NSUInteger)bytes_written;
}

- (NSTimeInterval)encodedDuration
{
    return total_samples / (NSTimeInterval)coding_rate;
}

@end

/*
 Comments will be stored in the Vorbis style.
 It is describled in the "Structure" section of
    http://www.xiph.org/ogg/vorbis/doc/v-comment.html

 However, Opus and other non-vorbis formats omit the "framing_bit".

The comment header is decoded as follows:
  1) [vendor_length] = read an unsigned integer of 32 bits
  2) [vendor_string] = read a UTF-8 vector as [vendor_length] octets
  3) [user_comment_list_length] = read an unsigned integer of 32 bits
  4) iterate [user_comment_list_length] times {
     5) [length] = read an unsigned integer of 32 bits
     6) this iteration's user comment = read a UTF-8 vector as [length] octets
     }
  7) done.
*/

#define readint(buf, base) (((buf[base+3]<<24)&0xff000000)| \
                           ((buf[base+2]<<16)&0xff0000)| \
                           ((buf[base+1]<<8)&0xff00)| \
                           (buf[base]&0xff))
#define writeint(buf, base, val) do{ buf[base+3]=((val)>>24)&0xff; \
                                     buf[base+2]=((val)>>16)&0xff; \
                                     buf[base+1]=((val)>>8)&0xff; \
                                     buf[base]=(val)&0xff; \
                                 }while(0)

static bool comment_init(char **comments, int *length, const char *vendor_string)
{
    // The 'vendor' field should be the actual encoding library used
    int vendor_length = strlen(vendor_string);
    int user_comment_list_length = 0;
    int len = 8 + 4 + vendor_length + 4;
    char *p = (char *)malloc(len);
    memcpy(p, "OpusTags", 8);
    writeint(p, 8, vendor_length);
    memcpy(p + 12, vendor_string, vendor_length);
    writeint(p, 12 + vendor_length, user_comment_list_length);
    *length = len;
    *comments = p;
    
    return true;
}

bool comment_add(char **comments, int* length, char *tag, char *val)
{
    char *p = *comments;
    int vendor_length = readint(p, 8);
    int user_comment_list_length = readint(p, 8 + 4 + vendor_length);
    int tag_len = (tag ? strlen(tag) + 1 : 0);
    int val_len = strlen(val);
    int len = (*length) + 4 + tag_len + val_len;
    
    p = (char *)realloc(p, len);
    
    writeint(p, *length, tag_len+val_len);      /* length of comment */
    if (tag)
    {
        memcpy(p + *length + 4, tag, tag_len);        /* comment tag */
        (p+*length+4)[tag_len-1] = '=';           /* separator */
    }
    memcpy(p + *length + 4 + tag_len, val, val_len);  /* comment */
    writeint(p, 8 + 4 + vendor_length, user_comment_list_length + 1);
    *comments = p;
    *length = len;
    
    return true;
}

static bool comment_pad(char **comments, int* length, int amount)
{
    if (amount > 0)
    {
        char *p = *comments;
        // Make sure there is at least amount worth of padding free, and round up to the maximum that fits in the current ogg segments
        int newlen = (*length + amount + 255) / 255 * 255 - 1;
        p = realloc(p, newlen);
        for (int i = *length; i < newlen; i++)
        {
            p[i] = 0;
        }
        *comments = p;
        *length = newlen;
    }
    
    return true;
}

#undef readint
#undef writeint
