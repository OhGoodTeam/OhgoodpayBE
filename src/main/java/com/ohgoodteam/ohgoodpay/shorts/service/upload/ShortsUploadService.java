package com.ohgoodteam.ohgoodpay.shorts.service.upload;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.ohgoodteam.ohgoodpay.shorts.dto.response.upload.ShortsUploadResponseDTO;

public interface ShortsUploadService {

    /**
     * 비디오 업로드
     * @param file
     * @param thumbnail
     * @param title
     * @param content
     * @return
     * @throws IOException
     */
    ShortsUploadResponseDTO upload(MultipartFile file, MultipartFile thumbnail, String title, String content, Long customerId) throws IOException;

}