#!/bin/bash
awk '
/imageVector = if \(post.isLiked\) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,/ {
    print "                            painter = if (post.isLiked) androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_like) else androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_like),"
    next
}
/imageVector = Icons.Default.ChatBubbleOutline,/ {
    print "                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_comment),"
    next
}
/imageVector = Icons.Default.Share,/ {
    print "                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_share),"
    next
}
{ print }
' app/src/main/java/com/example/ui/components/PostItemCard.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/components/PostItemCard.kt
