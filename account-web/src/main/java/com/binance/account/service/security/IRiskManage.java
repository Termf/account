package com.binance.account.service.security;


import com.binance.risk.vo.OperationType;

/**
 * @author liliang1
 * @date 2018-09-20 15:58
 */
public interface IRiskManage {

    /**
     * 证件类型的黑名单检查, 如果在黑名单中，风控会禁止用户提币
     * @param userId
     * @param idType
     * @param idNumber
     * @param country
     * @param operationType
     * @param
     */
    boolean checkIdNumberBackList(Long userId, String idType, String idNumber, String country, OperationType operationType, String jumioFacePath);


    /**
     *
     * 将用户face建立index，用于比对。 image里需要有清晰的人脸。
     *
     * @param s3Key 图片在S3的key。由于aws sdk不支持从S3直接indexFaces到rekognition, 所以需要先下载图片到本地再调用rekognition indexFaces接口。
     */
    void indexFaceIfNeeded(String s3Key);
}
