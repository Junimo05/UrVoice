const admin = require('firebase-admin');
const {onRequest} = require("firebase-functions/v2/https");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
admin.initializeApp();

exports.sendLikeNotification = onDocumentCreated('likes/{likeId}', async (event) => {
    try {
      const like = event.data.data();
      const userId = like.userID;
      const postId = like.postID;
  
      const userDoc = await admin.firestore().collection('users').doc(userId).get();
      if (!userDoc.exists) {
        throw new Error('User document does not exist');
      }
      const deviceToken = userDoc.data().deviceToken;
  
      const postDoc = await admin.firestore().collection('posts').doc(postId).get();
      if (!postDoc.exists) {
        throw new Error('Post document does not exist');
      }
  
      const user = userDoc.data();
      const post = postDoc.data();
      const payload = {
        notification: {
          title: 'New Like!',
          body: `${user.username} liked your post.`,
          clickAction: 'FLUTTER_NOTIFICATION_CLICK',
        },
        data: {
          postId: postId,
        },
      };
  
      const response = await admin.messaging().sendToDevice(deviceToken, payload);
      console.log('Notification sent successfully:', response);
    } catch (error) {
      console.error('Error sending notification:', error);
    }
  });