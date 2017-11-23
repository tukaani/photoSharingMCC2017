package com.appspot.mccfall2017g12.backend;

import com.google.cloud.vision.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageDetector {

    public static boolean detectFace(Image image) throws Exception {
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {

            List<AnnotateImageRequest> requests = new ArrayList<>();

            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.FACE_DETECTION)
                    .setMaxResults(1)
                    .build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            requests.add(request);

            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);

            return response.getResponses(0).getFaceAnnotationsCount() > 0;
        }
    }
}
