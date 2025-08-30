package com.example.MpFitness.Services;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException{
        Map uploadResultado = cloudinary.uploader().upload(file.getBytes(),Map.of());

        return (String) uploadResultado.get("secure_url");
    }
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, Map.of());
    }
    
}
