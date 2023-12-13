package dev.mikita.userservice.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import dev.mikita.userservice.exception.NotFoundException;
import dev.mikita.userservice.util.FirebaseStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * The type User service.
 */
@Service
public class UserService {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseStorageUtil firebaseStorageUtil;

    /**
     * Instantiates a new User service.
     *
     * @param firebaseAuth    the firebase auth
     */
    @Autowired
    public UserService(FirebaseAuth firebaseAuth, FirebaseStorageUtil firebaseStorageUtil) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseStorageUtil = firebaseStorageUtil;
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

        if (photoFile == null) {
            throw new IOException("File is null.");
        }

        // Get current photo URL
        String currentPhotoUrl = userRecord.getPhotoUrl();

        // Delete photo from Firebase Storage if it exists
        if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty()) {
            firebaseStorageUtil.deleteFile(firebaseStorageUtil.parseFileName(currentPhotoUrl));
        }

        String storagePath = firebaseStorageUtil.uploadImage(photoFile, "users/%s/".formatted(uid));

        // Update user
        UpdateRequest newPhotoRequest = new UpdateRequest(uid).setPhotoUrl(storagePath);
        firebaseAuth.updateUser(newPhotoRequest);
    }
}
