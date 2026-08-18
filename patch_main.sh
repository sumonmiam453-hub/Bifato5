#!/bin/bash
awk '
/import com.example.ui.components.FacebookHeader/ {
    print $0
    print "import com.example.ui.components.FacebookBottomNav"
    next
}
/FacebookHeader\(/ {
    in_header = 1
    print "                                FacebookHeader("
    print "                                    onSearchClick = { viewModel.setSearchActive(true) },"
    print "                                    onFriendClick = { viewModel.setSelectedTab(1) },"
    print "                                    onMenuClick = { viewModel.setSelectedTab(5) },"
    print "                                    onNotificationClick = { viewModel.setShowNotificationsScreen(true) },"
    print "                                    unreadNotificationsCount = unreadCount"
    print "                                )"
    next
}
in_header && /\)/ {
    if ($0 ~ /unreadNotificationsCount/ || $0 ~ /\)/) {
        if ($0 ~ /\)/ && $0 !~ /FacebookHeader/) {
            in_header = 0
        }
    }
    next
}
in_header { next }

/FacebookTabBar\(/ {
    in_tabbar = 1
    next
}
in_tabbar && /\)/ {
    in_tabbar = 0
    next
}
in_tabbar { next }

/topBar = \{/ {
    print "                    bottomBar = {"
    print "                        FacebookBottomNav("
    print "                            selectedTab = selectedTab,"
    print "                            onTabSelected = { tab -> viewModel.setSelectedTab(tab) },"
    print "                            onCreatePostClick = { viewModel.setCreatePostDialogVisible(true) },"
    print "                            onMessengerClick = { viewModel.setMessengerDrawerVisible(true) },"
    print "                            userAvatarUrl = userProfile?.avatarUrl"
    print "                        )"
    print "                    },"
    print $0
    next
}
{ print }
' app/src/main/java/com/example/MainActivity.kt > temp.kt && mv temp.kt app/src/main/java/com/example/MainActivity.kt
