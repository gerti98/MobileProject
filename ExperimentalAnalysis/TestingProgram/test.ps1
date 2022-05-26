adb shell dumpsys batterystats --reset
$num = 1

while ($num -le 3){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "BDBbrnoEVjgBtiAwuaQ3ZVRR3RE3"
    timeout 10
    adb shell input keyevent 4
    $num++
}

# adb report bugreport.zip