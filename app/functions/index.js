const { onSchedule } = require("firebase-functions/v2/scheduler");
const { initializeApp } = require("firebase-admin/app");
const admin = require("firebase-admin");

//Se debe volver a desplegar el servicio de firebase, para que se pueda usar
//Navegar hasta carpeta functions, ejecutar: firebase deploy --only functions
//Registro en cloud 
//https://console.cloud.google.com/logs/query;query=%2528resource.type%3D%22cloud_function%22%20resource.labels.function_name%3D%2528%22checkUnreadMessages%22%2529%20resource.labels.region%3D%22us-central1%22%2529%20OR%20%2528resource.type%3D%22cloud_run_revision%22%20resource.labels.service_name%3D%2528%22checkunreadmessages%22%2529%20resource.labels.location%3D%22us-central1%22%2529;cursorTimestamp=2025-06-26T20:39:01.810964Z;duration=PT1H?authuser=0&project=compulab-ea5eb&hl=es-419&inv=1&invt=Ab1LdA

initializeApp();

exports.checkUnreadMessages = onSchedule("every 525600 minutes", async (event) => {
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
