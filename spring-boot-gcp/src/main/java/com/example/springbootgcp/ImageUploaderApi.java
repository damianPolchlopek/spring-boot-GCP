package com.example.springbootgcp;

import com.google.cloud.storage.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.StreamSupport;

@RestController
public class ImageUploaderApi {

    private static final String BUCKET = "";
    Storage storage = StorageOptions.getDefaultInstance().getService();

    @PostMapping("/upload-file")
    public void uploadFile(@RequestParam("file")MultipartFile multipartFile) throws IOException {
        BlobId blobId = BlobId.of(BUCKET, multipartFile.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpg").build();
        storage.create(blobInfo, multipartFile.getBytes());
    }

    @GetMapping("/get-all-files")
    public List<String> getFile(){
        Iterable<Blob> blobs = storage.get(BUCKET).list().iterateAll();
        return StreamSupport.stream(blobs.spliterator(), false)
                .map(blob -> blob.getName())
                .collect(Collectors.toList());
    }

    @GetMapping("/get-file/{file:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String file){
        Blob blob = storage.get(BUCKET, file);
        byte[] bytes = storage.readAllBytes(BUCKET, file);
        Resource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok().
                contentType(MediaType.valueOf(blob.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"file\"; filename=\"{file}\"".replace("{file}", file))
                .body(resource);
    }

}
