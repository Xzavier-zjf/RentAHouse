package com.example.rentalhouse.service;

import com.example.rentalcommon.util.FileUploadValidator;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MongoFileStorageService {

    private final GridFsTemplate gridFsTemplate;

    public MongoFileStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String storeHouseImage(MultipartFile file) throws IOException {
        FileUploadValidator.requireValidImage(file);
        Document metadata = new Document("module", "house")
                .append("type", "house-image")
                .append("contentType", file.getContentType());
        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        );
        return "/api/house/file/" + fileId.toHexString();
    }

    public GridFsResource load(String fileId) {
        GridFSFile file = gridFsTemplate.findOne(
                Query.query(Criteria.where("_id").is(new ObjectId(fileId)))
        );
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        return gridFsTemplate.getResource(file);
    }
}
