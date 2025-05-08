const { onSchedule } = require("firebase-functions/v2/scheduler");
const { initializeApp } = require("firebase-admin/app");
const admin = require("firebase-admin");

initializeApp();

exports.checkUnreadMessages = onSchedule("every 7200 minutes", async (event) => {
  try {
    const usersSnapshot = await admin.firestore().collection("users").get();

    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;
      const fcmToken = userDoc.data().fcmToken;

      if (!fcmToken) {
        console.warn(`Usuario ${userId} no tiene fcmToken`);
        continue;
      }

      const messagesSnapshot = await admin
        .firestore()
        .collection(`message/${userId}/notifications`)
        .where("status", "==", 0)
        .get();

      for (const msgDoc of messagesSnapshot.docs) {
        const msgData = msgDoc.data();

        if (msgData.notified) {
          continue;
        }

        const title = msgData.subject || "Nuevo mensaje";
        const body = msgData.message || "Tienes una nueva actualización";

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

          // Evita reenvíos marcando como notificado
          await msgDoc.ref.update({ notified: true });

        } catch (sendError) {
          console.error(`Error al enviar notificación al usuario ${userId}:`, sendError);
        }
      }
    }
  } catch (error) {
    console.error("Error en la función programada:", error);
  }
});
