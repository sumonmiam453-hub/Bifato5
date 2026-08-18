#!/bin/bash
DIR="app/src/main/res/drawable"
mkdir -p $DIR

function write_icon() {
    local name=$1
    local pathdata=$2
    local extra=$3
    cat << XML > "$DIR/$name.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:strokeWidth="2.5" android:strokeColor="#FF000000" android:fillColor="#00000000" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="$pathdata" />
    $extra
</vector>
XML
}

write_icon "ic_bold_home" "M3,9.5 L12,3 L21,9.5 M5,8.5 V20 A1,1 0 0,0 6,21 H18 A1,1 0 0,0 19,20 V8.5 M9,21 V12 H15 V21" ""
write_icon "ic_bold_friends" "M16,21 V19 A4,4 0 0,0 12,15 H6 A4,4 0 0,0 2,19 V21 M9,11 A4,4 0 1,0 9,3 A4,4 0 0,0 9,11 Z M22,21 V19 A4,4 0 0,0 19,15.1 M16,3.1 A4,4 0 0,1 16,10.9" ""
write_icon "ic_bold_chat" "M12,21 C10.3,21 8.7,20.5 7.4,19.7 L3,21 L4.3,16.6 C3.5,15.3 3,13.7 3,12 C3,6.5 7.03,2 12,2 C16.97,2 21,6.5 21,12 C21,17.5 16.97,21 12,21 Z" "<circle cx=\"8\" cy=\"12\" r=\"1.8\" android:fillColor=\"#FF000000\" /><circle cx=\"12\" cy=\"12\" r=\"1.8\" android:fillColor=\"#FF000000\" /><circle cx=\"16\" cy=\"12\" r=\"1.8\" android:fillColor=\"#FF000000\" />"
write_icon "ic_bold_profile" "M20,21 V19 A4,4 0 0,0 16,15 H8 A4,4 0 0,0 4,19 V21 M12,11 A4,4 0 1,0 12,3 A4,4 0 0,0 12,11 Z" ""
write_icon "ic_bold_share" "M7,20 C7,11 11,8 20,8 M13,2 L21,8 L13,14" ""
write_icon "ic_bold_comment" "M20,4 A2,2 0 0,0 18,2 H6 A2,2 0 0,0 4,4 V14 A2,2 0 0,0 6,16 H8 V21 L14,16 H18 A2,2 0 0,0 20,14 Z" ""
write_icon "ic_bold_like" "M10,9 C11.5,7.5 12,5 12,3 A1.5,1.5 0 0,1 15,4 C15,6 14,9 17,9 H21.5 A2.5,2.5 0 0,1 24,11.5 L22,19 A2.5,2.5 0 0,1 19.5,21 H10 Z M10,21 H5 A2,2 0 0,1 3,19 V11 A2,2 0 0,1 5,9 H10" ""
write_icon "ic_bold_reels" "M4,4 H20 A2,2 0 0,1 22,6 V18 A2,2 0 0,1 20,20 H4 A2,2 0 0,1 2,18 V6 A2,2 0 0,1 4,4 Z M2,8 H22 M8,4 V8 M16,4 V8 M10,10 V16 L15,13 Z" ""
write_icon "ic_bold_menu" "M3,7 H21 M3,12 H21 M3,17 H21" ""
write_icon "ic_bold_bell" "M18,16 H6 C5,16 4.5,15.5 4.5,14.5 C5.5,12.5 5.5,9.5 5.5,9 A6.5,6.5 0 0,1 18.5,9 C18.5,9.5 18.5,12.5 19.5,14.5 C19.5,15.5 19,16 18,16 Z M9,16 A3,3 0 0,0 15,16 M12,3 V4" ""
write_icon "ic_bold_search" "M11,19 A8,8 0 1,0 11,3 A8,8 0 0,0 11,19 Z M21,21 L16.65,16.65" ""
