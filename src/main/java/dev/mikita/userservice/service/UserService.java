package dev.mikita.userservice.service;

import com.google.cloud.storage.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.google.firebase.cloud.StorageClient;
import dev.mikita.userservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

/**
 * The type User service.
 */
@Service
public class UserService {
    private final FirebaseAuth firebaseAuth;
    private final StorageClient firebaseStorage;

    /**
     * Instantiates a new User service.
     *
     * @param firebaseAuth    the firebase auth
     * @param firebaseStorage the firebase storage
     */
    @Autowired
    public UserService(FirebaseAuth firebaseAuth, StorageClient firebaseStorage) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseStorage = firebaseStorage;
    }

    /**
     * Update user photo.
     *
     * @param uid       the uid
     * @param photoFile the photo file
     * @throws IOException           the io exception
     * @throws FirebaseAuthException the firebase auth exception
     */
    public void updateUserPhoto(String uid, MultipartFile photoFile) throws IOException, FirebaseAuthException {
        // Get user
        UserRecord userRecord = firebaseAuth.getUser(uid);
        if (userRecord == null) {
            throw new NotFoundException("User not found.");
        }

        // Get current photo URL
        String currentPhotoUrl = userRecord.getPhotoUrl();
        Bucket bucket = firebaseStorage.bucket();

        // Delete photo from Firebase Storage if it exists
        if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty()) {
            String[] parts = currentPhotoUrl.split("/");
            String filePath = parts[parts.length - 2] + "/" + parts[parts.length - 1];

            Blob blob = bucket.get(filePath);

            if (blob != null) {
                blob.delete();
            }

            UpdateRequest updateRequest = new UpdateRequest(uid).setPhotoUrl(null);
            firebaseAuth.updateUser(updateRequest);
        } else {
            throw new NotFoundException("User does not have a photo.");
        }

        // Upload new photo to Firebase Storage
        // Photo name
        String originalFilename = photoFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Original filename is null");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFilename = UUID.randomUUID() + fileExtension;

        // Firebase Storage
        String storagePath = "users/" + uniqueFilename;
        bucket.create(storagePath, photoFile.getBytes(), photoFile.getContentType());
        String photoUrl = "https://storage.googleapis.com/" + bucket.getName() + "/" + storagePath;

        // Update user
        UpdateRequest newPhotoRequest = new UpdateRequest(uid).setPhotoUrl(photoUrl);
        firebaseAuth.updateUser(newPhotoRequest);
    }
}
