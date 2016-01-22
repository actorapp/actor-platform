//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MobileCoreServices

let UTTAll = [
    kUTTypeItem,
]

let AAFileTypes = [
    "mp3"  :AAFileType.Music,
    "m4a"  :AAFileType.Music,
    "ogg"  :AAFileType.Music,
    "flac" :AAFileType.Music,
    "alac" :AAFileType.Music,
    "wav"  :AAFileType.Music,
    "wma"  :AAFileType.Music,
    "aac"  :AAFileType.Music,
    
    "doc"  :AAFileType.Doc,
    "docm" :AAFileType.Doc,
    "dot"  :AAFileType.Doc,
    "dotx" :AAFileType.Doc,
    "epub" :AAFileType.Doc,
    "fb2"  :AAFileType.Doc,
    "xml"  :AAFileType.Doc,
    "info" :AAFileType.Doc,
    "tex"  :AAFileType.Doc,
    "stw"  :AAFileType.Doc,
    "sxw"  :AAFileType.Doc,
    "txt"  :AAFileType.Doc,
    "xlc"  :AAFileType.Doc,
    "odf"  :AAFileType.Doc,
    "odt"  :AAFileType.Doc,
    "ott"  :AAFileType.Doc,
    "rtf"  :AAFileType.Doc,
    "pages":AAFileType.Doc,
    "ini"  :AAFileType.Doc,

    "xls"  :AAFileType.Spreadsheet,
    "xlsx" :AAFileType.Spreadsheet,
    "xlsm" :AAFileType.Spreadsheet,
    "xlsb" :AAFileType.Spreadsheet,
    "numbers":AAFileType.Spreadsheet,

    "jpg"  :AAFileType.Picture,
    "jpeg"  :AAFileType.Picture,
    "jp2"  :AAFileType.Picture,
    "jps"  :AAFileType.Picture,
    "gif"  :AAFileType.Picture,
    "tiff"  :AAFileType.Picture,
    "png"  :AAFileType.Picture,
    "psd"  :AAFileType.Picture,
    "webp"  :AAFileType.Picture,
    "ico"  :AAFileType.Picture,
    "pcx"  :AAFileType.Picture,
    "tga"  :AAFileType.Picture,
    "raw"  :AAFileType.Picture,
    "svg"  :AAFileType.Picture,
    
    "mp4"  :AAFileType.Video,
    "3gp"  :AAFileType.Video,
    "m4v"  :AAFileType.Video,
    "webm"  :AAFileType.Video,
    
    "ppt"  :AAFileType.Presentation,
    "key"  :AAFileType.Presentation,
    "keynote"  :AAFileType.Presentation,
    
    "pdf"  :AAFileType.PDF,
    "apk"  :AAFileType.APK,
    "rar"  :AAFileType.RAR,
    "zip"  :AAFileType.ZIP,
    "csv"  :AAFileType.CSV,
    
    "xhtm"  :AAFileType.HTML,
    "htm"  :AAFileType.HTML,
    "html"  :AAFileType.HTML,
]

enum AAFileType {
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