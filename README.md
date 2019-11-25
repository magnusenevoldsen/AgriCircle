# AgriCircle

The login page needs a SensitiveInfo.kt file.
This file contains personal login info.
Either delete the lines, or create the object as following:

```
package com.magnusenevoldsen.agricircle
object SensitiveInfo {
    fun returnEmail(chosenUser : Int) : String {
        return if (chosenUser == 1) "Insert email 1"
        else return "Insert email 2"
    }
    fun returnPassword(chosenUser : Int) : String {
        return if (chosenUser == 1) "Insert password 1"
        else return "Insert password 2"
    }
}
```
