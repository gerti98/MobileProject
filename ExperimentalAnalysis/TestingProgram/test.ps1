# 1. Change branch to 'testing_branch' 
# 2. Link your phone with Wireless debugging and run the app
# 3. Login from the app with any account
# 4. Run the script and wait patiently

$i = 1
$single_test_repetitions = 80

# ---------------- For testing purposes -------------------

adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
timeout 15
adb shell input keyevent 4

adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "MHZsUPUgc0OrK8JDpzts1N27neA3"
timeout 15
adb shell input keyevent 4

adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "p7nq5IXiNGXJ4aSOoD34E2iuKkk1"
timeout 15
adb shell input keyevent 4

adb bugreport results/to_be_deleted.zip

# ---------------- Experiments -------------------

adb shell dumpsys batterystats --reset
while ($i -le $single_test_repetitions){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
    timeout 15
    adb shell input keyevent 4
    $i++
}

echo "Finished test ONLY MESSAGES"
$i = 1
adb bugreport results/bugreport_auto_test_only_messages.zip

adb shell dumpsys batterystats --reset
while ($i -le $single_test_repetitions){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "MHZsUPUgc0OrK8JDpzts1N27neA3"
    timeout 15
    adb shell input keyevent 4
    $i++
}

echo "Finished test ONE AUDIO"
$i = 1
adb bugreport results/bugreport_auto_test_one_audio.zip

adb shell dumpsys batterystats --reset
while ($i -le $single_test_repetitions){
    adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "p7nq5IXiNGXJ4aSOoD34E2iuKkk1"
    timeout 15
    adb shell input keyevent 4
    $i++
}

echo "Finished test THREE AUDIO"
adb bugreport results/bugreport_auto_three_audio.zip