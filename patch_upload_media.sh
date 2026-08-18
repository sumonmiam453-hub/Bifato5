awk '/\/\/ -------------------------------------------------------------/{
    if (!inserted) {
        print "    suspend fun uploadMedia(localPathOrUri: String): String? {"
        print "        if (localPathOrUri.isBlank()) return null"
        print "        if (localPathOrUri.startsWith(\"http\")) return localPathOrUri"
        print "        if (localPathOrUri.startsWith(\"data:image\")) return localPathOrUri"
        print "        return try {"
        print "            val fileUri = if (localPathOrUri.startsWith(\"content://\")) {"
        print "                Uri.parse(localPathOrUri)"
        print "            } else {"
        print "                Uri.fromFile(File(localPathOrUri))"
        print "            }"
        print "            val ref = storage.reference.child(\"media/${UUID.randomUUID()}\")"
        print "            ref.putFile(fileUri).await()"
        print "            ref.downloadUrl.await().toString()"
        print "        } catch (e: Exception) {"
        print "            Log.e(\"FirebaseManager\", \"Error uploading media\", e)"
        print "            null"
        print "        }"
        print "    }"
        print ""
        inserted = 1
    }
}
{print}' app/src/main/java/com/example/data/FirebaseManager.kt > temp.kt && mv temp.kt app/src/main/java/com/example/data/FirebaseManager.kt
