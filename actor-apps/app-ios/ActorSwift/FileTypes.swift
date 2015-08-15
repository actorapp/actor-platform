//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import MobileCoreServices

let UTTAll = [
    kUTTypeItem,
]

let FileTypes = [
    "mp3"  :FileType.Music,
    "m4a"  :FileType.Music,
    "ogg"  :FileType.Music,
    "flac" :FileType.Music,
    "alac" :FileType.Music,
    "wav"  :FileType.Music,
    "wma"  :FileType.Music,
    "aac"  :FileType.Music,
    
    "doc"  :FileType.Doc,
    "docm" :FileType.Doc,
    "dot"  :FileType.Doc,
    "dotx" :FileType.Doc,
    "epub" :FileType.Doc,
    "fb2"  :FileType.Doc,
    "xml"  :FileType.Doc,
    "info" :FileType.Doc,
    "tex"  :FileType.Doc,
    "stw"  :FileType.Doc,
    "sxw"  :FileType.Doc,
    "txt"  :FileType.Doc,
    "xlc"  :FileType.Doc,
    "odf"  :FileType.Doc,
    "odt"  :FileType.Doc,
    "ott"  :FileType.Doc,
    "rtf"  :FileType.Doc,
    "pages":FileType.Doc,
    "ini"  :FileType.Doc,

    "xls"  :FileType.Spreadsheet,
    "xlsx" :FileType.Spreadsheet,
    "xlsm" :FileType.Spreadsheet,
    "xlsb" :FileType.Spreadsheet,
    "numbers":FileType.Spreadsheet,

    "jpg"  :FileType.Picture,
    "jpeg"  :FileType.Picture,
    "jp2"  :FileType.Picture,
    "jps"  :FileType.Picture,
    "gif"  :FileType.Picture,
    "tiff"  :FileType.Picture,
    "png"  :FileType.Picture,
    "psd"  :FileType.Picture,
    "webp"  :FileType.Picture,
    "ico"  :FileType.Picture,
    "pcx"  :FileType.Picture,
    "tga"  :FileType.Picture,
    "raw"  :FileType.Picture,
    "svg"  :FileType.Picture,
    
    "mp4"  :FileType.Video,
    "3gp"  :FileType.Video,
    "m4v"  :FileType.Video,
    "webm"  :FileType.Video,
    
    "ppt"  :FileType.Presentation,
    "key"  :FileType.Presentation,
    "keynote"  :FileType.Presentation,
    
    "pdf"  :FileType.PDF,
    "apk"  :FileType.APK,
    "rar"  :FileType.RAR,
    "zip"  :FileType.ZIP,
    "csv"  :FileType.CSV,
    
    "xhtm"  :FileType.HTML,
    "htm"  :FileType.HTML,
    "html"  :FileType.HTML,
]

enum FileType {
    case Music
    case Doc
    case Spreadsheet
    case Picture
    case Video
    case Presentation
    case PDF
    case APK
    case RAR
    case ZIP
    case CSV
    case HTML
    case UNKNOWN
}