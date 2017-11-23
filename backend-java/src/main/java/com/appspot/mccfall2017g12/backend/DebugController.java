package com.appspot.mccfall2017g12.backend;

import com.google.cloud.vision.v1.Image;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DebugController {
    @PostMapping("/photoorganizer/api/v3.0/process")
    public boolean processImage(@RequestParam("file") MultipartFile file) throws Exception {

        Image image = Image.parseFrom(file.getInputStream());

        return ImageDetector.detectFace(image);
    }
}
