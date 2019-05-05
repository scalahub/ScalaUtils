
package org.sh.utils.common.file.prop

import org.sh.utils.common.json.JSONUtil.JsonFormatted

object PropDataStructures {
  case class PropVal(key:String, value:Any) extends JsonFormatted {
    val keys = Array(key)
    val vals = Array(value)
  }
  case class FileInfo(fileName:String, isEncrypted:Boolean) extends JsonFormatted {
    val keys = Array("fileName", "isEncrypted")
    val vals = Array(fileName, isEncrypted)
  }
  case class PropInfo(propName:String, fileName:String, isEncrypted:Boolean) extends JsonFormatted {
    val keys = Array("propName", "fileName":String, "isEncrypted")
    val vals = Array(propName, fileName:String, isEncrypted)
  }
}
