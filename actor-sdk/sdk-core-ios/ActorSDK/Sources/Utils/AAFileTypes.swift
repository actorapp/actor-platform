//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MobileCoreServices

let UTTAll = [
    kUTTypeItem,
]

let AAFileTypes = [
    "mp3"  :AAFileType.music,
    "m4a"  :AAFileType.music,
    "ogg"  :AAFileType.music,
    "flac" :AAFileType.music,
    "alac" :AAFileType.music,
    "wav"  :AAFileType.music,
    "wma"  :AAFileType.music,
    "aac"  :AAFileType.music,
    
    "doc"  :AAFileType.doc,
    "docm" :AAFileType.doc,
    "dot"  :AAFileType.doc,
    "dotx" :AAFileType.doc,
    "epub" :AAFileType.doc,
    "fb2"  :AAFileType.doc,
    "xml"  :AAFileType.doc,
    "info" :AAFileType.doc,
    "tex"  :AAFileType.doc,
    "stw"  :AAFileType.doc,
    "sxw"  :AAFileType.doc,
    "txt"  :AAFileType.doc,
    "xlc"  :AAFileType.doc,
    "odf"  :AAFileType.doc,
    "odt"  :AAFileType.doc,
    "ott"  :AAFileType.doc,
    "rtf"  :AAFileType.doc,
    "pages":AAFileType.doc,
    "ini"  :AAFileType.doc,

    "xls"  :AAFileType.spreadsheet,
    "xlsx" :AAFileType.spreadsheet,
    "xlsm" :AAFileType.spreadsheet,
    "xlsb" :AAFileType.spreadsheet,
    "numbers":AAFileType.spreadsheet,

    "jpg"  :AAFileType.picture,
    "jpeg"  :AAFileType.picture,
    "jp2"  :AAFileType.picture,
    "jps"  :AAFileType.picture,
    "gif"  :AAFileType.picture,
    "tiff"  :AAFileType.picture,
    "png"  :AAFileType.picture,
    "psd"  :AAFileType.picture,
    "webp"  :AAFileType.picture,
    "ico"  :AAFileType.picture,
    "pcx"  :AAFileType.picture,
    "tga"  :AAFileType.picture,
    "raw"  :AAFileType.picture,
    "svg"  :AAFileType.picture,
    
    "mp4"  :AAFileType.video,
    "3gp"  :AAFileType.video,
    "m4v"  :AAFileType.video,
    "webm"  :AAFileType.video,
    
    "ppt"  :AAFileType.presentation,
    "key"  :AAFileType.presentation,
    "keynote"  :AAFileType.presentation,
    
    "pdf"  :AAFileType.pdf,
    "apk"  :AAFileType.apk,
    "rar"  :AAFileType.rar,
    "zip"  :AAFileType.zip,
    "csv"  :AAFileType.csv,
    
    "xhtm"  :AAFileType.html,
    "htm"  :AAFileType.html,
    "html"  :AAFileType.html,
]

enum AAFileType {
    case music
    case doc
    case spreadsheet
    case picture
    case video
    case presentation
    case pdf
    case apk
    case rar
    case zip
    case csv
    case html
    case unknown
}
