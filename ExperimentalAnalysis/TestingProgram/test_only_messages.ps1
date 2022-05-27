# 1. Change branch to 'testing_branch' 
# 2. Link your phone with Wireless debugging and run the app
# 3. Login from the app with any account
# 4. Run the script and wait patiently

$i = 1
$single_test_repetitions = 3

    
adb shell dumpsys batterystats --reset
while ($i -le $single_test_repetitions){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
    timeout 10
    adb shell input keyevent 4
    $i++
}

echo "Finished test ONLY MESSAGES"
$i = 0
adb bugreport results/bugreport_auto_test_only_messages_$j.zip

adb shell dumpsys batterystats --reset
while ($i -le $single_test_repetitions){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
    timeout 10
    adb shell input keyevent 4
    $i++
}

echo "Finished test ONLY MESSAGES"
$i = 0
adb bugreport results/bugreport_auto_test_only_messages_$j.zip

adb shell dumpsys batterystats --reset
while ($i -le $single_test_repetitions){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
    timeout 10
    adb shell input keyevent 4
    $i++
}

echo "Finished test ONLY MESSAGES"
$i = 0
adb bugreport results/bugreport_auto_test_only_messages_$j.zip