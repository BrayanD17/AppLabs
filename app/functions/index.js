const { onSchedule } = require("firebase-functions/v2/scheduler");
const { initializeApp } = require("firebase-admin/app");
const admin = require("firebase-admin");

initializeApp();

exports.checkUnreadMessages = onSchedule("every 3 minutes", async (event) => {
  try {
    const usersSnapshot = await admin.firestore().collection("users").get();

    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;
      const fcmToken = userDoc.data().fcmToken;

      if (!fcmToken) {
        console.warn(`Usuario ${userId} no tiene fcmToken`);
        continue;
      }

      //Access the messages collection, to listen for changes in the state
      const messagesSnapshot = await admin
        .firestore()
        .collection(`message/${userId}/notifications`)
        .where("status", "==", 0)
        .get();

      for (const msgDoc of messagesSnapshot.docs) {
        const msgData = msgDoc.data();

        //Marking as notified, so it is not sent again
        if (msgData.notified) {
          continue;
        }

        //Structend for sending the notification
        const title = msgData.subject || "Mensaje nuevo";
        const body = msgData.message || "Actualización en solicitud";

        const notification = {
          notification: {
            title,
            body,
          },
          data: {
            title,
            body,
          },
          token: fcmToken,
          android: {
            priority: "high",
          },
        };

        try {
          await admin.messaging().send(notification);
          console.log(`Notificación enviada al usuario ${userId}`);

          //Marking as notified, so it is not sent again
          await msgDoc.ref.update({ notified: true });

        } catch (sendError) {
          console.error(`Error al enviar notificación al usuario ${userId}:`, sendError);
        }
      }
    }
  } catch (error) {
    console.error("Error en el disparador de notificaciones:", error);
  }
});
