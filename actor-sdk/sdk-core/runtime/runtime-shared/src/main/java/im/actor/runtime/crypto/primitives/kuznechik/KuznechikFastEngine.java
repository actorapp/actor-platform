package im.actor.runtime.crypto.primitives.kuznechik;

import im.actor.runtime.Crypto;
import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.primitives.util.Pack;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ALL_CHECKS 1
]-*/

public class KuznechikFastEngine implements BlockCipher {

    private static final byte[] kuz_pi = new byte[]{
            (byte) 0xFC, (byte) 0xEE, (byte) 0xDD, (byte) 0x11, (byte) 0xCF, (byte) 0x6E, (byte) 0x31, (byte) 0x16,    // 00..07
            (byte) 0xFB, (byte) 0xC4, (byte) 0xFA, (byte) 0xDA, (byte) 0x23, (byte) 0xC5, (byte) 0x04, (byte) 0x4D,    // 08..0F
            (byte) 0xE9, (byte) 0x77, (byte) 0xF0, (byte) 0xDB, (byte) 0x93, (byte) 0x2E, (byte) 0x99, (byte) 0xBA,    // 10..17
            (byte) 0x17, (byte) 0x36, (byte) 0xF1, (byte) 0xBB, (byte) 0x14, (byte) 0xCD, (byte) 0x5F, (byte) 0xC1,    // 18..1F
            (byte) 0xF9, (byte) 0x18, (byte) 0x65, (byte) 0x5A, (byte) 0xE2, (byte) 0x5C, (byte) 0xEF, (byte) 0x21,    // 20..27
            (byte) 0x81, (byte) 0x1C, (byte) 0x3C, (byte) 0x42, (byte) 0x8B, (byte) 0x01, (byte) 0x8E, (byte) 0x4F,    // 28..2F
            (byte) 0x05, (byte) 0x84, (byte) 0x02, (byte) 0xAE, (byte) 0xE3, (byte) 0x6A, (byte) 0x8F, (byte) 0xA0,    // 30..37
            (byte) 0x06, (byte) 0x0B, (byte) 0xED, (byte) 0x98, (byte) 0x7F, (byte) 0xD4, (byte) 0xD3, (byte) 0x1F,    // 38..3F
            (byte) 0xEB, (byte) 0x34, (byte) 0x2C, (byte) 0x51, (byte) 0xEA, (byte) 0xC8, (byte) 0x48, (byte) 0xAB,    // 40..47
            (byte) 0xF2, (byte) 0x2A, (byte) 0x68, (byte) 0xA2, (byte) 0xFD, (byte) 0x3A, (byte) 0xCE, (byte) 0xCC,    // 48..4F
            (byte) 0xB5, (byte) 0x70, (byte) 0x0E, (byte) 0x56, (byte) 0x08, (byte) 0x0C, (byte) 0x76, (byte) 0x12,    // 50..57
            (byte) 0xBF, (byte) 0x72, (byte) 0x13, (byte) 0x47, (byte) 0x9C, (byte) 0xB7, (byte) 0x5D, (byte) 0x87,    // 58..5F
            (byte) 0x15, (byte) 0xA1, (byte) 0x96, (byte) 0x29, (byte) 0x10, (byte) 0x7B, (byte) 0x9A, (byte) 0xC7,    // 60..67
            (byte) 0xF3, (byte) 0x91, (byte) 0x78, (byte) 0x6F, (byte) 0x9D, (byte) 0x9E, (byte) 0xB2, (byte) 0xB1,    // 68..6F
            (byte) 0x32, (byte) 0x75, (byte) 0x19, (byte) 0x3D, (byte) 0xFF, (byte) 0x35, (byte) 0x8A, (byte) 0x7E,    // 70..77
            (byte) 0x6D, (byte) 0x54, (byte) 0xC6, (byte) 0x80, (byte) 0xC3, (byte) 0xBD, (byte) 0x0D, (byte) 0x57,    // 78..7F
            (byte) 0xDF, (byte) 0xF5, (byte) 0x24, (byte) 0xA9, (byte) 0x3E, (byte) 0xA8, (byte) 0x43, (byte) 0xC9,    // 80..87
            (byte) 0xD7, (byte) 0x79, (byte) 0xD6, (byte) 0xF6, (byte) 0x7C, (byte) 0x22, (byte) 0xB9, (byte) 0x03,    // 88..8F
            (byte) 0xE0, (byte) 0x0F, (byte) 0xEC, (byte) 0xDE, (byte) 0x7A, (byte) 0x94, (byte) 0xB0, (byte) 0xBC,    // 90..97
            (byte) 0xDC, (byte) 0xE8, (byte) 0x28, (byte) 0x50, (byte) 0x4E, (byte) 0x33, (byte) 0x0A, (byte) 0x4A,    // 98..9F
            (byte) 0xA7, (byte) 0x97, (byte) 0x60, (byte) 0x73, (byte) 0x1E, (byte) 0x00, (byte) 0x62, (byte) 0x44,    // A0..A7
            (byte) 0x1A, (byte) 0xB8, (byte) 0x38, (byte) 0x82, (byte) 0x64, (byte) 0x9F, (byte) 0x26, (byte) 0x41,    // A8..AF
            (byte) 0xAD, (byte) 0x45, (byte) 0x46, (byte) 0x92, (byte) 0x27, (byte) 0x5E, (byte) 0x55, (byte) 0x2F,    // B0..B7
            (byte) 0x8C, (byte) 0xA3, (byte) 0xA5, (byte) 0x7D, (byte) 0x69, (byte) 0xD5, (byte) 0x95, (byte) 0x3B,    // B8..BF
            (byte) 0x07, (byte) 0x58, (byte) 0xB3, (byte) 0x40, (byte) 0x86, (byte) 0xAC, (byte) 0x1D, (byte) 0xF7,    // C0..C7
            (byte) 0x30, (byte) 0x37, (byte) 0x6B, (byte) 0xE4, (byte) 0x88, (byte) 0xD9, (byte) 0xE7, (byte) 0x89,    // C8..CF
            (byte) 0xE1, (byte) 0x1B, (byte) 0x83, (byte) 0x49, (byte) 0x4C, (byte) 0x3F, (byte) 0xF8, (byte) 0xFE,    // D0..D7
            (byte) 0x8D, (byte) 0x53, (byte) 0xAA, (byte) 0x90, (byte) 0xCA, (byte) 0xD8, (byte) 0x85, (byte) 0x61,    // D8..DF
            (byte) 0x20, (byte) 0x71, (byte) 0x67, (byte) 0xA4, (byte) 0x2D, (byte) 0x2B, (byte) 0x09, (byte) 0x5B,    // E0..E7
            (byte) 0xCB, (byte) 0x9B, (byte) 0x25, (byte) 0xD0, (byte) 0xBE, (byte) 0xE5, (byte) 0x6C, (byte) 0x52,    // E8..EF
            (byte) 0x59, (byte) 0xA6, (byte) 0x74, (byte) 0xD2, (byte) 0xE6, (byte) 0xF4, (byte) 0xB4, (byte) 0xC0,    // F0..F7
            (byte) 0xD1, (byte) 0x66, (byte) 0xAF, (byte) 0xC2, (byte) 0x39, (byte) 0x4B, (byte) 0x63, (byte) 0xB6,    // F8..FF
    };

    // Inverse S-Box
    private static final byte[] kuz_pi_inv = new byte[]{
            (byte) 0xA5, (byte) 0x2D, (byte) 0x32, (byte) 0x8F, (byte) 0x0E, (byte) 0x30, (byte) 0x38, (byte) 0xC0,    // 00..07
            (byte) 0x54, (byte) 0xE6, (byte) 0x9E, (byte) 0x39, (byte) 0x55, (byte) 0x7E, (byte) 0x52, (byte) 0x91,    // 08..0F
            (byte) 0x64, (byte) 0x03, (byte) 0x57, (byte) 0x5A, (byte) 0x1C, (byte) 0x60, (byte) 0x07, (byte) 0x18,    // 10..17
            (byte) 0x21, (byte) 0x72, (byte) 0xA8, (byte) 0xD1, (byte) 0x29, (byte) 0xC6, (byte) 0xA4, (byte) 0x3F,    // 18..1F
            (byte) 0xE0, (byte) 0x27, (byte) 0x8D, (byte) 0x0C, (byte) 0x82, (byte) 0xEA, (byte) 0xAE, (byte) 0xB4,    // 20..27
            (byte) 0x9A, (byte) 0x63, (byte) 0x49, (byte) 0xE5, (byte) 0x42, (byte) 0xE4, (byte) 0x15, (byte) 0xB7,    // 28..2F
            (byte) 0xC8, (byte) 0x06, (byte) 0x70, (byte) 0x9D, (byte) 0x41, (byte) 0x75, (byte) 0x19, (byte) 0xC9,    // 30..37
            (byte) 0xAA, (byte) 0xFC, (byte) 0x4D, (byte) 0xBF, (byte) 0x2A, (byte) 0x73, (byte) 0x84, (byte) 0xD5,    // 38..3F
            (byte) 0xC3, (byte) 0xAF, (byte) 0x2B, (byte) 0x86, (byte) 0xA7, (byte) 0xB1, (byte) 0xB2, (byte) 0x5B,    // 40..47
            (byte) 0x46, (byte) 0xD3, (byte) 0x9F, (byte) 0xFD, (byte) 0xD4, (byte) 0x0F, (byte) 0x9C, (byte) 0x2F,    // 48..4F
            (byte) 0x9B, (byte) 0x43, (byte) 0xEF, (byte) 0xD9, (byte) 0x79, (byte) 0xB6, (byte) 0x53, (byte) 0x7F,    // 50..57
            (byte) 0xC1, (byte) 0xF0, (byte) 0x23, (byte) 0xE7, (byte) 0x25, (byte) 0x5E, (byte) 0xB5, (byte) 0x1E,    // 58..5F
            (byte) 0xA2, (byte) 0xDF, (byte) 0xA6, (byte) 0xFE, (byte) 0xAC, (byte) 0x22, (byte) 0xF9, (byte) 0xE2,    // 60..67
            (byte) 0x4A, (byte) 0xBC, (byte) 0x35, (byte) 0xCA, (byte) 0xEE, (byte) 0x78, (byte) 0x05, (byte) 0x6B,    // 68..6F
            (byte) 0x51, (byte) 0xE1, (byte) 0x59, (byte) 0xA3, (byte) 0xF2, (byte) 0x71, (byte) 0x56, (byte) 0x11,    // 70..77
            (byte) 0x6A, (byte) 0x89, (byte) 0x94, (byte) 0x65, (byte) 0x8C, (byte) 0xBB, (byte) 0x77, (byte) 0x3C,    // 78..7F
            (byte) 0x7B, (byte) 0x28, (byte) 0xAB, (byte) 0xD2, (byte) 0x31, (byte) 0xDE, (byte) 0xC4, (byte) 0x5F,    // 80..87
            (byte) 0xCC, (byte) 0xCF, (byte) 0x76, (byte) 0x2C, (byte) 0xB8, (byte) 0xD8, (byte) 0x2E, (byte) 0x36,    // 88..8F
            (byte) 0xDB, (byte) 0x69, (byte) 0xB3, (byte) 0x14, (byte) 0x95, (byte) 0xBE, (byte) 0x62, (byte) 0xA1,    // 90..97
            (byte) 0x3B, (byte) 0x16, (byte) 0x66, (byte) 0xE9, (byte) 0x5C, (byte) 0x6C, (byte) 0x6D, (byte) 0xAD,    // 98..9F
            (byte) 0x37, (byte) 0x61, (byte) 0x4B, (byte) 0xB9, (byte) 0xE3, (byte) 0xBA, (byte) 0xF1, (byte) 0xA0,    // A0..A7
            (byte) 0x85, (byte) 0x83, (byte) 0xDA, (byte) 0x47, (byte) 0xC5, (byte) 0xB0, (byte) 0x33, (byte) 0xFA,    // A8..AF
            (byte) 0x96, (byte) 0x6F, (byte) 0x6E, (byte) 0xC2, (byte) 0xF6, (byte) 0x50, (byte) 0xFF, (byte) 0x5D,    // B0..B7
            (byte) 0xA9, (byte) 0x8E, (byte) 0x17, (byte) 0x1B, (byte) 0x97, (byte) 0x7D, (byte) 0xEC, (byte) 0x58,    // B8..BF
            (byte) 0xF7, (byte) 0x1F, (byte) 0xFB, (byte) 0x7C, (byte) 0x09, (byte) 0x0D, (byte) 0x7A, (byte) 0x67,    // C0..C7
            (byte) 0x45, (byte) 0x87, (byte) 0xDC, (byte) 0xE8, (byte) 0x4F, (byte) 0x1D, (byte) 0x4E, (byte) 0x04,    // C8..CF
            (byte) 0xEB, (byte) 0xF8, (byte) 0xF3, (byte) 0x3E, (byte) 0x3D, (byte) 0xBD, (byte) 0x8A, (byte) 0x88,    // D0..D7
            (byte) 0xDD, (byte) 0xCD, (byte) 0x0B, (byte) 0x13, (byte) 0x98, (byte) 0x02, (byte) 0x93, (byte) 0x80,    // D8..DF
            (byte) 0x90, (byte) 0xD0, (byte) 0x24, (byte) 0x34, (byte) 0xCB, (byte) 0xED, (byte) 0xF4, (byte) 0xCE,    // E0..E7
            (byte) 0x99, (byte) 0x10, (byte) 0x44, (byte) 0x40, (byte) 0x92, (byte) 0x3A, (byte) 0x01, (byte) 0x26,    // E8..EF
            (byte) 0x12, (byte) 0x1A, (byte) 0x48, (byte) 0x68, (byte) 0xF5, (byte) 0x81, (byte) 0x8B, (byte) 0xC7,    // F0..F7
            (byte) 0xD6, (byte) 0x20, (byte) 0x0A, (byte) 0x08, (byte) 0x00, (byte) 0x4C, (byte) 0xD7, (byte) 0x74        // F8..FF
    };

    // Linear vector from sect 5.1.2
    private static final byte[] kuz_lvec = new byte[]{
            (byte) 0x94, (byte) 0x20, (byte) 0x85, (byte) 0x10, (byte) 0xC2, (byte) 0xC0, (byte) 0x01, (byte) 0xFB,
            (byte) 0x01, (byte) 0xC0, (byte) 0xC2, (byte) 0x10, (byte) 0x85, (byte) 0x20, (byte) 0x94, (byte) 0x01
    };

    // Generated with http://www.cs.utsa.edu/~wagner/laws/FFM.html
    private static final byte[] gf256_E = new byte[]{
            (byte) 0x01, (byte) 0x03, (byte) 0x05, (byte) 0x0f, (byte) 0x11, (byte) 0x33, (byte) 0x55, (byte) 0xff, (byte) 0xc2, (byte) 0x85, (byte) 0x4c, (byte) 0xd4, (byte) 0xbf, (byte) 0x02, (byte) 0x06, (byte) 0x0a,
            (byte) 0x1e, (byte) 0x22, (byte) 0x66, (byte) 0xaa, (byte) 0x3d, (byte) 0x47, (byte) 0xc9, (byte) 0x98, (byte) 0x6b, (byte) 0xbd, (byte) 0x04, (byte) 0x0c, (byte) 0x14, (byte) 0x3c, (byte) 0x44, (byte) 0xcc,
            (byte) 0x97, (byte) 0x7a, (byte) 0x8e, (byte) 0x51, (byte) 0xf3, (byte) 0xd6, (byte) 0xb9, (byte) 0x08, (byte) 0x18, (byte) 0x28, (byte) 0x78, (byte) 0x88, (byte) 0x5b, (byte) 0xed, (byte) 0xf4, (byte) 0xdf,
            (byte) 0xa2, (byte) 0x25, (byte) 0x6f, (byte) 0xb1, (byte) 0x10, (byte) 0x30, (byte) 0x50, (byte) 0xf0, (byte) 0xd3, (byte) 0xb6, (byte) 0x19, (byte) 0x2b, (byte) 0x7d, (byte) 0x87, (byte) 0x4a, (byte) 0xde,
            (byte) 0xa1, (byte) 0x20, (byte) 0x60, (byte) 0xa0, (byte) 0x23, (byte) 0x65, (byte) 0xaf, (byte) 0x32, (byte) 0x56, (byte) 0xfa, (byte) 0xcd, (byte) 0x94, (byte) 0x7f, (byte) 0x81, (byte) 0x40, (byte) 0xc0,
            (byte) 0x83, (byte) 0x46, (byte) 0xca, (byte) 0x9d, (byte) 0x64, (byte) 0xac, (byte) 0x37, (byte) 0x59, (byte) 0xeb, (byte) 0xfe, (byte) 0xc1, (byte) 0x80, (byte) 0x43, (byte) 0xc5, (byte) 0x8c, (byte) 0x57,
            (byte) 0xf9, (byte) 0xc8, (byte) 0x9b, (byte) 0x6e, (byte) 0xb2, (byte) 0x15, (byte) 0x3f, (byte) 0x41, (byte) 0xc3, (byte) 0x86, (byte) 0x49, (byte) 0xdb, (byte) 0xae, (byte) 0x31, (byte) 0x53, (byte) 0xf5,
            (byte) 0xdc, (byte) 0xa7, (byte) 0x2a, (byte) 0x7e, (byte) 0x82, (byte) 0x45, (byte) 0xcf, (byte) 0x92, (byte) 0x75, (byte) 0x9f, (byte) 0x62, (byte) 0xa6, (byte) 0x29, (byte) 0x7b, (byte) 0x8d, (byte) 0x54,
            (byte) 0xfc, (byte) 0xc7, (byte) 0x8a, (byte) 0x5d, (byte) 0xe7, (byte) 0xea, (byte) 0xfd, (byte) 0xc4, (byte) 0x8f, (byte) 0x52, (byte) 0xf6, (byte) 0xd9, (byte) 0xa8, (byte) 0x3b, (byte) 0x4d, (byte) 0xd7,
            (byte) 0xba, (byte) 0x0d, (byte) 0x17, (byte) 0x39, (byte) 0x4b, (byte) 0xdd, (byte) 0xa4, (byte) 0x2f, (byte) 0x71, (byte) 0x93, (byte) 0x76, (byte) 0x9a, (byte) 0x6d, (byte) 0xb7, (byte) 0x1a, (byte) 0x2e,
            (byte) 0x72, (byte) 0x96, (byte) 0x79, (byte) 0x8b, (byte) 0x5e, (byte) 0xe2, (byte) 0xe5, (byte) 0xec, (byte) 0xf7, (byte) 0xda, (byte) 0xad, (byte) 0x34, (byte) 0x5c, (byte) 0xe4, (byte) 0xef, (byte) 0xf2,
            (byte) 0xd5, (byte) 0xbc, (byte) 0x07, (byte) 0x09, (byte) 0x1b, (byte) 0x2d, (byte) 0x77, (byte) 0x99, (byte) 0x68, (byte) 0xb8, (byte) 0x0b, (byte) 0x1d, (byte) 0x27, (byte) 0x69, (byte) 0xbb, (byte) 0x0e,
            (byte) 0x12, (byte) 0x36, (byte) 0x5a, (byte) 0xee, (byte) 0xf1, (byte) 0xd0, (byte) 0xb3, (byte) 0x16, (byte) 0x3a, (byte) 0x4e, (byte) 0xd2, (byte) 0xb5, (byte) 0x1c, (byte) 0x24, (byte) 0x6c, (byte) 0xb4,
            (byte) 0x1f, (byte) 0x21, (byte) 0x63, (byte) 0xa5, (byte) 0x2c, (byte) 0x74, (byte) 0x9c, (byte) 0x67, (byte) 0xa9, (byte) 0x38, (byte) 0x48, (byte) 0xd8, (byte) 0xab, (byte) 0x3e, (byte) 0x42, (byte) 0xc6,
            (byte) 0x89, (byte) 0x58, (byte) 0xe8, (byte) 0xfb, (byte) 0xce, (byte) 0x91, (byte) 0x70, (byte) 0x90, (byte) 0x73, (byte) 0x95, (byte) 0x7c, (byte) 0x84, (byte) 0x4f, (byte) 0xd1, (byte) 0xb0, (byte) 0x13,
            (byte) 0x35, (byte) 0x5f, (byte) 0xe1, (byte) 0xe0, (byte) 0xe3, (byte) 0xe6, (byte) 0xe9, (byte) 0xf8, (byte) 0xcb, (byte) 0x9e, (byte) 0x61, (byte) 0xa3, (byte) 0x26, (byte) 0x6a, (byte) 0xbe, (byte) 0x01,
    };

    // Generated with http://www.cs.utsa.edu/~wagner/laws/FFM.html
    private static final byte[] gf256_L = new byte[]{
            (byte) 0x00, (byte) 0x00, (byte) 0x0d, (byte) 0x01, (byte) 0x1a, (byte) 0x02, (byte) 0x0e, (byte) 0xb2, (byte) 0x27, (byte) 0xb3, (byte) 0x0f, (byte) 0xba, (byte) 0x1b, (byte) 0x91, (byte) 0xbf, (byte) 0x03,
            (byte) 0x34, (byte) 0x04, (byte) 0xc0, (byte) 0xef, (byte) 0x1c, (byte) 0x65, (byte) 0xc7, (byte) 0x92, (byte) 0x28, (byte) 0x3a, (byte) 0x9e, (byte) 0xb4, (byte) 0xcc, (byte) 0xbb, (byte) 0x10, (byte) 0xd0,
            (byte) 0x41, (byte) 0xd1, (byte) 0x11, (byte) 0x44, (byte) 0xcd, (byte) 0x31, (byte) 0xfc, (byte) 0xbc, (byte) 0x29, (byte) 0x7c, (byte) 0x72, (byte) 0x3b, (byte) 0xd4, (byte) 0xb5, (byte) 0x9f, (byte) 0x97,
            (byte) 0x35, (byte) 0x6d, (byte) 0x47, (byte) 0x05, (byte) 0xab, (byte) 0xf0, (byte) 0xc1, (byte) 0x56, (byte) 0xd9, (byte) 0x93, (byte) 0xc8, (byte) 0x8d, (byte) 0x1d, (byte) 0x14, (byte) 0xdd, (byte) 0x66,
            (byte) 0x4e, (byte) 0x67, (byte) 0xde, (byte) 0x5c, (byte) 0x1e, (byte) 0x75, (byte) 0x51, (byte) 0x15, (byte) 0xda, (byte) 0x6a, (byte) 0x3e, (byte) 0x94, (byte) 0x0a, (byte) 0x8e, (byte) 0xc9, (byte) 0xec,
            (byte) 0x36, (byte) 0x23, (byte) 0x89, (byte) 0x6e, (byte) 0x7f, (byte) 0x06, (byte) 0x48, (byte) 0x5f, (byte) 0xe1, (byte) 0x57, (byte) 0xc2, (byte) 0x2c, (byte) 0xac, (byte) 0x83, (byte) 0xa4, (byte) 0xf1,
            (byte) 0x42, (byte) 0xfa, (byte) 0x7a, (byte) 0xd2, (byte) 0x54, (byte) 0x45, (byte) 0x12, (byte) 0xd7, (byte) 0xb8, (byte) 0xbd, (byte) 0xfd, (byte) 0x18, (byte) 0xce, (byte) 0x9c, (byte) 0x63, (byte) 0x32,
            (byte) 0xe6, (byte) 0x98, (byte) 0xa0, (byte) 0xe8, (byte) 0xd5, (byte) 0x78, (byte) 0x9a, (byte) 0xb6, (byte) 0x2a, (byte) 0xa2, (byte) 0x21, (byte) 0x7d, (byte) 0xea, (byte) 0x3c, (byte) 0x73, (byte) 0x4c,
            (byte) 0x5b, (byte) 0x4d, (byte) 0x74, (byte) 0x50, (byte) 0xeb, (byte) 0x09, (byte) 0x69, (byte) 0x3d, (byte) 0x2b, (byte) 0xe0, (byte) 0x82, (byte) 0xa3, (byte) 0x5e, (byte) 0x7e, (byte) 0x22, (byte) 0x88,
            (byte) 0xe7, (byte) 0xe5, (byte) 0x77, (byte) 0x99, (byte) 0x4b, (byte) 0xe9, (byte) 0xa1, (byte) 0x20, (byte) 0x17, (byte) 0xb7, (byte) 0x9b, (byte) 0x62, (byte) 0xd6, (byte) 0x53, (byte) 0xf9, (byte) 0x79,
            (byte) 0x43, (byte) 0x40, (byte) 0x30, (byte) 0xfb, (byte) 0x96, (byte) 0xd3, (byte) 0x7b, (byte) 0x71, (byte) 0x8c, (byte) 0xd8, (byte) 0x13, (byte) 0xdc, (byte) 0x55, (byte) 0xaa, (byte) 0x6c, (byte) 0x46,
            (byte) 0xee, (byte) 0x33, (byte) 0x64, (byte) 0xc6, (byte) 0xcf, (byte) 0xcb, (byte) 0x39, (byte) 0x9d, (byte) 0xb9, (byte) 0x26, (byte) 0x90, (byte) 0xbe, (byte) 0xb1, (byte) 0x19, (byte) 0xfe, (byte) 0x0c,
            (byte) 0x4f, (byte) 0x5a, (byte) 0x08, (byte) 0x68, (byte) 0x87, (byte) 0x5d, (byte) 0xdf, (byte) 0x81, (byte) 0x61, (byte) 0x16, (byte) 0x52, (byte) 0xf8, (byte) 0x1f, (byte) 0x4a, (byte) 0xe4, (byte) 0x76,
            (byte) 0xc5, (byte) 0xed, (byte) 0xca, (byte) 0x38, (byte) 0x0b, (byte) 0xb0, (byte) 0x25, (byte) 0x8f, (byte) 0xdb, (byte) 0x8b, (byte) 0xa9, (byte) 0x6b, (byte) 0x70, (byte) 0x95, (byte) 0x3f, (byte) 0x2f,
            (byte) 0xf3, (byte) 0xf2, (byte) 0xa5, (byte) 0xf4, (byte) 0xad, (byte) 0xa6, (byte) 0xf5, (byte) 0x84, (byte) 0xe2, (byte) 0xf6, (byte) 0x85, (byte) 0x58, (byte) 0xa7, (byte) 0x2d, (byte) 0xc3, (byte) 0xae,
            (byte) 0x37, (byte) 0xc4, (byte) 0xaf, (byte) 0x24, (byte) 0x2e, (byte) 0x6f, (byte) 0x8a, (byte) 0xa8, (byte) 0xf7, (byte) 0x60, (byte) 0x49, (byte) 0xe3, (byte) 0x80, (byte) 0x86, (byte) 0x59, (byte) 0x07,
    };

    private static int[] gf256res;
    private static int[] gf256resInv;

    public static void initCalc() {
        if (gf256res != null || gf256resInv != null) {
            return;
        }

        gf256res = new int[16 * 256 * 4];
        gf256resInv = new int[16 * 256 * 4];

        byte[] tmp = new byte[16];
        for (int index = 0; index < 16; index++) {
            for (int i = 0; i < 256; i++) {
                for (int l = 0; l < 16; l++) {
                    tmp[l] = 0;
                }
                tmp[index] = (byte) i;
                kuz_l(tmp);
                Pack.bigEndianToInt(tmp, 0, gf256res, (index + (16 * i)) * 4, 4);

                for (int l = 0; l < 16; l++) {
                    tmp[l] = 0;
                }
                tmp[index] = (byte) i;
                kuz_l_inv(tmp);
                Pack.bigEndianToInt(tmp, 0, gf256resInv, (index + (16 * i)) * 4, 4);
            }
        }
    }

    public static void initDump(final byte[] data) {
        if (gf256res != null || gf256resInv != null) {
            return;
        }
        gf256res = new int[16 * 256 * 4];
        gf256resInv = new int[16 * 256 * 4];
        int offset = 0;
        int n;
        for (int i = 0; i < gf256res.length; ++i) {
            n = data[offset++] << 24;
            n |= (data[offset++] & 0xff) << 16;
            n |= (data[offset++] & 0xff) << 8;
            n |= (data[offset++] & 0xff);
            gf256res[i] = n;
        }

        for (int i = 0; i < gf256resInv.length; ++i) {
            n = data[offset++] << 24;
            n |= (data[offset++] & 0xff) << 16;
            n |= (data[offset++] & 0xff) << 8;
            n |= (data[offset++] & 0xff);
            gf256resInv[i] = n;
        }
    }

    static void kuz_l_fast(int[] w) {
        int a0 = 0, a1 = 0, a2 = 0, a3 = 0;
        for (int ind = 0; ind < 16; ind++) {
            int dataByte = (ind + ((w[ind / 4] >> (3 - ind % 4) * 8) & 0xFF) * 16) * 4;

            a0 = a0 ^ gf256res[dataByte + 0];
            a1 = a1 ^ gf256res[dataByte + 1];
            a2 = a2 ^ gf256res[dataByte + 2];
            a3 = a3 ^ gf256res[dataByte + 3];
        }
        w[0] = a0;
        w[1] = a1;
        w[2] = a2;
        w[3] = a3;
    }

    static void kuz_l(byte[] w) {
        for (int j = 0; j < 16; j++) {
            byte x = w[15];
            w[15] = w[14];
            x ^= kuz_mul_gf256_fast(w[14], kuz_lvec[14]);
            w[14] = w[13];
            x ^= kuz_mul_gf256_fast(w[13], kuz_lvec[13]);
            w[13] = w[12];
            x ^= kuz_mul_gf256_fast(w[12], kuz_lvec[12]);
            w[12] = w[11];
            x ^= kuz_mul_gf256_fast(w[11], kuz_lvec[11]);
            w[11] = w[10];
            x ^= kuz_mul_gf256_fast(w[10], kuz_lvec[10]);
            w[10] = w[9];
            x ^= kuz_mul_gf256_fast(w[9], kuz_lvec[9]);
            w[9] = w[8];
            x ^= kuz_mul_gf256_fast(w[8], kuz_lvec[8]);
            w[8] = w[7];
            x ^= kuz_mul_gf256_fast(w[7], kuz_lvec[7]);
            w[7] = w[6];
            x ^= kuz_mul_gf256_fast(w[6], kuz_lvec[6]);
            w[6] = w[5];
            x ^= kuz_mul_gf256_fast(w[5], kuz_lvec[5]);
            w[5] = w[4];
            x ^= kuz_mul_gf256_fast(w[4], kuz_lvec[4]);
            w[4] = w[3];
            x ^= kuz_mul_gf256_fast(w[3], kuz_lvec[3]);
            w[3] = w[2];
            x ^= kuz_mul_gf256_fast(w[2], kuz_lvec[2]);
            w[2] = w[1];
            x ^= kuz_mul_gf256_fast(w[1], kuz_lvec[1]);
            w[1] = w[0];
            x ^= kuz_mul_gf256_fast(w[0], kuz_lvec[0]);
            w[0] = x;
        }
    }

    static void kuz_l_inv(byte[] w) {
        for (int j = 0; j < 16; j++) {
            byte x = w[0];
            for (int i = 0; i < 15; i++) {
                w[i] = w[i + 1];
                x ^= kuz_mul_gf256_fast(w[i], kuz_lvec[i]);
            }
            w[15] = x;
        }
    }

    static byte kuz_mul_gf256_fast(byte a, byte b) {
        if (a == 0 || b == 0) return 0;
        int t = (gf256_L[(a & 0xff)] & 0xff) + (gf256_L[(b & 0xff)] & 0xff);
        if (t > 255) t = t - 255;
        return gf256_E[(t & 0xff)];
    }

    private static final int BLOCK_SIZE = 16;

    private int[][] key;
    private int C0;
    private int C1;
    private int C2;
    private int C3;

    public KuznechikFastEngine(byte[] key) {
        Crypto.waitForCryptoLoaded();
        this.key = convertKey(key);
    }

    @Override
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public void encryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {

        int A0, A1, A2, A3, T0, T1, T2, T3;

        unpackBlock(data, offset);

        for (int i = 0; i < 9; i++) {

            C0 = C0 ^ key[i][0];
            C1 = C1 ^ key[i][1];
            C2 = C2 ^ key[i][2];
            C3 = C3 ^ key[i][3];

            C0 = (kuz_pi[C0 & 0xFF] & 0xFF)
                    + ((kuz_pi[(C0 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(C0 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(C0 >> 24) & 0xFF] & 0xFF) << 24);

            C1 = (kuz_pi[C1 & 0xFF] & 0xFF)
                    + ((kuz_pi[(C1 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(C1 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(C1 >> 24) & 0xFF] & 0xFF) << 24);

            C2 = (kuz_pi[C2 & 0xFF] & 0xFF)
                    + ((kuz_pi[(C2 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(C2 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(C2 >> 24) & 0xFF] & 0xFF) << 24);

            C3 = (kuz_pi[C3 & 0xFF] & 0xFF)
                    + ((kuz_pi[(C3 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(C3 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(C3 >> 24) & 0xFF] & 0xFF) << 24);

            T0 = (0 + (((C0 >> 24) & 0xFF) << 4)) << 2;
            T1 = (1 + (((C0 >> 16) & 0xFF) << 4)) << 2;
            T2 = (2 + (((C0 >> 8) & 0xFF) << 4)) << 2;
            T3 = (3 + (((C0 >> 0) & 0xFF) << 4)) << 2;

            A0 = gf256res[T0 + 0] ^ gf256res[T1 + 0] ^ gf256res[T2 + 0] ^ gf256res[T3 + 0];
            A1 = gf256res[T0 + 1] ^ gf256res[T1 + 1] ^ gf256res[T2 + 1] ^ gf256res[T3 + 1];
            A2 = gf256res[T0 + 2] ^ gf256res[T1 + 2] ^ gf256res[T2 + 2] ^ gf256res[T3 + 2];
            A3 = gf256res[T0 + 3] ^ gf256res[T1 + 3] ^ gf256res[T2 + 3] ^ gf256res[T3 + 3];


            T0 = (4 + (((C1 >> 24) & 0xFF) << 4)) << 2;
            T1 = (5 + (((C1 >> 16) & 0xFF) << 4)) << 2;
            T2 = (6 + (((C1 >> 8) & 0xFF) << 4)) << 2;
            T3 = (7 + (((C1 >> 0) & 0xFF) << 4)) << 2;
            A0 = A0 ^ gf256res[T0 + 0] ^ gf256res[T1 + 0] ^ gf256res[T2 + 0] ^ gf256res[T3 + 0];
            A1 = A1 ^ gf256res[T0 + 1] ^ gf256res[T1 + 1] ^ gf256res[T2 + 1] ^ gf256res[T3 + 1];
            A2 = A2 ^ gf256res[T0 + 2] ^ gf256res[T1 + 2] ^ gf256res[T2 + 2] ^ gf256res[T3 + 2];
            A3 = A3 ^ gf256res[T0 + 3] ^ gf256res[T1 + 3] ^ gf256res[T2 + 3] ^ gf256res[T3 + 3];


            T0 = (8 + (((C2 >> 24) & 0xFF) << 4)) << 2;
            T1 = (9 + (((C2 >> 16) & 0xFF) << 4)) << 2;
            T2 = (10 + (((C2 >> 8) & 0xFF) << 4)) << 2;
            T3 = (11 + (((C2 >> 0) & 0xFF) << 4)) << 2;
            A0 = A0 ^ gf256res[T0 + 0] ^ gf256res[T1 + 0] ^ gf256res[T2 + 0] ^ gf256res[T3 + 0];
            A1 = A1 ^ gf256res[T0 + 1] ^ gf256res[T1 + 1] ^ gf256res[T2 + 1] ^ gf256res[T3 + 1];
            A2 = A2 ^ gf256res[T0 + 2] ^ gf256res[T1 + 2] ^ gf256res[T2 + 2] ^ gf256res[T3 + 2];
            A3 = A3 ^ gf256res[T0 + 3] ^ gf256res[T1 + 3] ^ gf256res[T2 + 3] ^ gf256res[T3 + 3];


            T0 = (12 + (((C3 >> 24) & 0xFF) << 4)) << 2;
            T1 = (13 + (((C3 >> 16) & 0xFF) << 4)) << 2;
            T2 = (14 + (((C3 >> 8) & 0xFF) << 4)) << 2;
            T3 = (15 + (((C3 >> 0) & 0xFF) << 4)) << 2;
            C0 = A0 ^ gf256res[T0 + 0] ^ gf256res[T1 + 0] ^ gf256res[T2 + 0] ^ gf256res[T3 + 0];
            C1 = A1 ^ gf256res[T0 + 1] ^ gf256res[T1 + 1] ^ gf256res[T2 + 1] ^ gf256res[T3 + 1];
            C2 = A2 ^ gf256res[T0 + 2] ^ gf256res[T1 + 2] ^ gf256res[T2 + 2] ^ gf256res[T3 + 2];
            C3 = A3 ^ gf256res[T0 + 3] ^ gf256res[T1 + 3] ^ gf256res[T2 + 3] ^ gf256res[T3 + 3];
        }

        C0 = C0 ^ key[9][0];
        C1 = C1 ^ key[9][1];
        C2 = C2 ^ key[9][2];
        C3 = C3 ^ key[9][3];

        packBlock(dest, destOffset);
    }

    @Override
    public void decryptBlock(byte[] data, int offset, byte[] dest, int destOffset) {

        int A0, A1, A2, A3, T0, T1, T2, T3;

        unpackBlock(data, offset);

        C0 = C0 ^ key[9][0];
        C1 = C1 ^ key[9][1];
        C2 = C2 ^ key[9][2];
        C3 = C3 ^ key[9][3];

        for (int i = 8; i >= 0; i--) {

            T0 = (0 + (((C0 >> 24) & 0xFF) << 4)) << 2;
            T1 = (1 + (((C0 >> 16) & 0xFF) << 4)) << 2;
            T2 = (2 + (((C0 >> 8) & 0xFF) << 4)) << 2;
            T3 = (3 + (((C0 >> 0) & 0xFF) << 4)) << 2;

            A0 = gf256resInv[T0 + 0] ^ gf256resInv[T1 + 0] ^ gf256resInv[T2 + 0] ^ gf256resInv[T3 + 0];
            A1 = gf256resInv[T0 + 1] ^ gf256resInv[T1 + 1] ^ gf256resInv[T2 + 1] ^ gf256resInv[T3 + 1];
            A2 = gf256resInv[T0 + 2] ^ gf256resInv[T1 + 2] ^ gf256resInv[T2 + 2] ^ gf256resInv[T3 + 2];
            A3 = gf256resInv[T0 + 3] ^ gf256resInv[T1 + 3] ^ gf256resInv[T2 + 3] ^ gf256resInv[T3 + 3];


            T0 = (4 + (((C1 >> 24) & 0xFF) << 4)) << 2;
            T1 = (5 + (((C1 >> 16) & 0xFF) << 4)) << 2;
            T2 = (6 + (((C1 >> 8) & 0xFF) << 4)) << 2;
            T3 = (7 + (((C1 >> 0) & 0xFF) << 4)) << 2;
            A0 = A0 ^ gf256resInv[T0 + 0] ^ gf256resInv[T1 + 0] ^ gf256resInv[T2 + 0] ^ gf256resInv[T3 + 0];
            A1 = A1 ^ gf256resInv[T0 + 1] ^ gf256resInv[T1 + 1] ^ gf256resInv[T2 + 1] ^ gf256resInv[T3 + 1];
            A2 = A2 ^ gf256resInv[T0 + 2] ^ gf256resInv[T1 + 2] ^ gf256resInv[T2 + 2] ^ gf256resInv[T3 + 2];
            A3 = A3 ^ gf256resInv[T0 + 3] ^ gf256resInv[T1 + 3] ^ gf256resInv[T2 + 3] ^ gf256resInv[T3 + 3];


            T0 = (8 + (((C2 >> 24) & 0xFF) << 4)) << 2;
            T1 = (9 + (((C2 >> 16) & 0xFF) << 4)) << 2;
            T2 = (10 + (((C2 >> 8) & 0xFF) << 4)) << 2;
            T3 = (11 + (((C2 >> 0) & 0xFF) << 4)) << 2;
            A0 = A0 ^ gf256resInv[T0 + 0] ^ gf256resInv[T1 + 0] ^ gf256resInv[T2 + 0] ^ gf256resInv[T3 + 0];
            A1 = A1 ^ gf256resInv[T0 + 1] ^ gf256resInv[T1 + 1] ^ gf256resInv[T2 + 1] ^ gf256resInv[T3 + 1];
            A2 = A2 ^ gf256resInv[T0 + 2] ^ gf256resInv[T1 + 2] ^ gf256resInv[T2 + 2] ^ gf256resInv[T3 + 2];
            A3 = A3 ^ gf256resInv[T0 + 3] ^ gf256resInv[T1 + 3] ^ gf256resInv[T2 + 3] ^ gf256resInv[T3 + 3];


            T0 = (12 + (((C3 >> 24) & 0xFF) << 4)) << 2;
            T1 = (13 + (((C3 >> 16) & 0xFF) << 4)) << 2;
            T2 = (14 + (((C3 >> 8) & 0xFF) << 4)) << 2;
            T3 = (15 + (((C3 >> 0) & 0xFF) << 4)) << 2;
            C0 = A0 ^ gf256resInv[T0 + 0] ^ gf256resInv[T1 + 0] ^ gf256resInv[T2 + 0] ^ gf256resInv[T3 + 0];
            C1 = A1 ^ gf256resInv[T0 + 1] ^ gf256resInv[T1 + 1] ^ gf256resInv[T2 + 1] ^ gf256resInv[T3 + 1];
            C2 = A2 ^ gf256resInv[T0 + 2] ^ gf256resInv[T1 + 2] ^ gf256resInv[T2 + 2] ^ gf256resInv[T3 + 2];
            C3 = A3 ^ gf256resInv[T0 + 3] ^ gf256resInv[T1 + 3] ^ gf256resInv[T2 + 3] ^ gf256resInv[T3 + 3];


            C0 = (kuz_pi_inv[C0 & 0xFF] & 0xFF)
                    + ((kuz_pi_inv[(C0 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi_inv[(C0 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi_inv[(C0 >> 24) & 0xFF] & 0xFF) << 24);

            C1 = (kuz_pi_inv[C1 & 0xFF] & 0xFF)
                    + ((kuz_pi_inv[(C1 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi_inv[(C1 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi_inv[(C1 >> 24) & 0xFF] & 0xFF) << 24);

            C2 = (kuz_pi_inv[C2 & 0xFF] & 0xFF)
                    + ((kuz_pi_inv[(C2 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi_inv[(C2 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi_inv[(C2 >> 24) & 0xFF] & 0xFF) << 24);

            C3 = (kuz_pi_inv[C3 & 0xFF] & 0xFF)
                    + ((kuz_pi_inv[(C3 >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi_inv[(C3 >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi_inv[(C3 >> 24) & 0xFF] & 0xFF) << 24);


            C0 = C0 ^ key[i][0];
            C1 = C1 ^ key[i][1];
            C2 = C2 ^ key[i][2];
            C3 = C3 ^ key[i][3];
        }

        packBlock(dest, destOffset);
    }

    private void unpackBlock(byte[] bytes, int off) {
        this.C0 = Pack.bigEndianToInt(bytes, off);
        this.C1 = Pack.bigEndianToInt(bytes, off + 4);
        this.C2 = Pack.bigEndianToInt(bytes, off + 8);
        this.C3 = Pack.bigEndianToInt(bytes, off + 12);
    }

    private void packBlock(byte[] dest, int destOffset) {
        Pack.intToBigEndian(C0, dest, destOffset);
        Pack.intToBigEndian(C1, dest, destOffset + 4);
        Pack.intToBigEndian(C2, dest, destOffset + 8);
        Pack.intToBigEndian(C3, dest, destOffset + 12);
    }

    int[][] getKey() {
        return key;
    }

    static int[][] convertKey(byte[] key) {
        if (key.length != 32) {
            throw new RuntimeException("Key might be 32 bytes length");
        }

        int[][] kuz = new int[10][4];

        // w128_t c, x, y, z;
        int[] c = new int[4];
        int[] x = new int[4];
        int[] y = new int[4];
        int[] z = new int[4];

        kuz[0][0] = x[0] = Pack.bigEndianToInt(key, 0);
        kuz[0][1] = x[1] = Pack.bigEndianToInt(key, 4);
        kuz[0][2] = x[2] = Pack.bigEndianToInt(key, 8);
        kuz[0][3] = x[3] = Pack.bigEndianToInt(key, 12);

        kuz[1][0] = y[0] = Pack.bigEndianToInt(key, 16);
        kuz[1][1] = y[1] = Pack.bigEndianToInt(key, 20);
        kuz[1][2] = y[2] = Pack.bigEndianToInt(key, 24);
        kuz[1][3] = y[3] = Pack.bigEndianToInt(key, 28);

        for (int i = 1; i <= 32; i++) {

            c[0] = 0;
            c[1] = 0;
            c[2] = 0;
            c[3] = i;

            kuz_l_fast(c);

            z[0] = x[0] ^ c[0];
            z[1] = x[1] ^ c[1];
            z[2] = x[2] ^ c[2];
            z[3] = x[3] ^ c[3];

            z[0] = (kuz_pi[z[0] & 0xFF] & 0xFF)
                    + ((kuz_pi[(z[0] >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(z[0] >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(z[0] >> 24) & 0xFF] & 0xFF) << 24);

            z[1] = (kuz_pi[z[1] & 0xFF] & 0xFF)
                    + ((kuz_pi[(z[1] >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(z[1] >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(z[1] >> 24) & 0xFF] & 0xFF) << 24);

            z[2] = (kuz_pi[z[2] & 0xFF] & 0xFF)
                    + ((kuz_pi[(z[2] >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(z[2] >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(z[2] >> 24) & 0xFF] & 0xFF) << 24);

            z[3] = (kuz_pi[z[3] & 0xFF] & 0xFF)
                    + ((kuz_pi[(z[3] >> 8) & 0xFF] & 0xFF) << 8)
                    + ((kuz_pi[(z[3] >> 16) & 0xFF] & 0xFF) << 16)
                    + ((kuz_pi[(z[3] >> 24) & 0xFF] & 0xFF) << 24);

            kuz_l_fast(z);

            z[0] = z[0] ^ y[0];
            z[1] = z[1] ^ y[1];
            z[2] = z[2] ^ y[2];
            z[3] = z[3] ^ y[3];

            y[0] = x[0];
            y[1] = x[1];
            y[2] = x[2];
            y[3] = x[3];

            x[0] = z[0];
            x[1] = z[1];
            x[2] = z[2];
            x[3] = z[3];

            if ((i & 7) == 0) {
                kuz[i >> 2][0] = x[0];
                kuz[i >> 2][1] = x[1];
                kuz[i >> 2][2] = x[2];
                kuz[i >> 2][3] = x[3];

                kuz[(i >> 2) + 1][0] = y[0];
                kuz[(i >> 2) + 1][1] = y[1];
                kuz[(i >> 2) + 1][2] = y[2];
                kuz[(i >> 2) + 1][3] = y[3];
            }
        }

        return kuz;
    }
}