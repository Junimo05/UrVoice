module.exports = async function sendNotiToTopic(topic, title, body) {
    // [START sendNotiToTopic]
    async function sendNotiToTopic(topic, title, body) {
        const message = {
          notification: { title: title, body: body },
          topic: topic,
          data: { click_action: 'FLUTTER_NOTIFICATION_CLICK' }
        };
        
        try {
          const response = await admin.messaging().send(message);
          console.log("Notification sent successfully", response);
        } catch (error) {
          console.error("Error sending notification: ", error);
        }
      }
    // [END sendNotiToTopic]
};