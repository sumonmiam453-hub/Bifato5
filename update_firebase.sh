#!/bin/bash
sed -i 's/import com.google.firebase.firestore.FirebaseFirestore/import com.google.firebase.firestore.FirebaseFirestore\nimport com.google.firebase.storage.FirebaseStorage\nimport java.io.File\nimport java.util.UUID/g' app/src/main/java/com/example/data/FirebaseManager.kt
