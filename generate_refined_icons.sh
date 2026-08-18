#!/bin/bash
DIR="app/src/main/res/drawable"
mkdir -p $DIR

function write_icon() {
    local name=$1
    local pathdata=$2
    local extra=$3
    local strokewidth=${4:-2.5}
    cat << XML > "$DIR/$name.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:strokeWidth="$strokewidth" android:strokeColor="#FF000000" android:fillColor="#00000000" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="$pathdata" />
    $extra
</vector>
XML
}

write_icon "ic_bold_share" "M 6,21 C 6,12 11,8 19,8 M 12,3 L 20,8 L 12,13" "" 3.0
write_icon "ic_bold_comment" "M 6,4 C 4.9,4 4,4.9 4,6 L 4,16 C 4,17.1 4.9,18 6,18 L 8,18 L 8,22 L 13,18 L 18,18 C 19.1,18 20,17.1 20,16 L 20,6 C 20,4.9 19.1,4 18,4 Z" "" 2.5
write_icon "ic_bold_like" "M 9,21 H 5 C 3.9,21 3,20.1 3,19 V 11 C 3,9.9 3.9,9 5,9 H 9 M 9,9 C 9,9 11,9 12,6 C 13,3 14,2 15,2 C 16.1,2 17,2.9 17,4 C 17,5.5 16,7 16,7 H 20 C 21.1,7 22,7.9 22,9 L 20,19 C 19.8,20.2 18.9,21 17.8,21 H 9" "" 2.5
write_icon "ic_bold_menu" "M 4,6 H 20 M 4,12 H 20 M 4,18 H 20" "" 3.0
write_icon "ic_bold_bell" "M 18,16 V 11 C 18,7.93 16.36,5.36 13.5,4.68 V 4 C 13.5,3.17 12.83,2.5 12,2.5 C 11.17,2.5 10.5,3.17 10.5,4 V 4.68 C 7.63,5.36 6,7.92 6,11 V 16 L 4,18 V 19 H 20 V 18 L 18,16 Z M 12,22 C 13.1,22 14,21.1 14,20 H 10 C 10,21.1 10.9,22 12,22 Z" "" 2.5
write_icon "ic_bold_search" "M 11,4 A 7,7 0 1,0 11,18 A 7,7 0 1,0 11,4 Z M 16,16 L 22,22" "" 3.0
write_icon "ic_bold_reels" "M 4,6 C 4,4.9 4.9,4 6,4 H 18 C 19.1,4 20,4.9 20,6 V 18 C 20,19.1 19.1,20 18,20 H 6 C 4.9,20 4,19.1 4,18 Z M 4,9 H 20 M 9,4 V 9 M 15,4 V 9 M 10,12 L 15,14.5 L 10,17 Z" "" 2.5
