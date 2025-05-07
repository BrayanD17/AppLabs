const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");

initializeApp();

exports.sendNotificationOnMessageCreate = onDocumentCreated(
  "message/{userId}/notifications/{messageId}",
  async (event) => {
    const snapshot = event.data;
    const data = snapshot.data();
    if (data.estado !== 0) return;

    const userId = event.params.userId;
    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) return;

    const token = userDoc.data().deviceToken;
    if (!token) return;

    await admin.messaging().send({
      notification: {
        title: data.titulo || "Nuevo mensaje",
        body: data.mensaje || "Tienes un nuevo mensaje.",
      },
      token: token,
    });
  }
);