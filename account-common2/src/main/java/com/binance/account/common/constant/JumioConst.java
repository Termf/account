package com.binance.account.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * const of jumio
 * @author: caixinning
 * @date: 2018/08/08 10:52
 **/
public final class JumioConst {

    public static final String STATUS_APPROVED_VERIFIED = "APPROVED_VERIFIED";
    public static final String STATUS_DENIED_FRAUD = "DENIED_FRAUD";
    public static final String STATUS_ERROR_NOT_READABLE_ID = "ERROR_NOT_READABLE_ID";
    public static final String ERROR_MSG_ID_NUMBER = "id.photo.authenticated";
    public static final String ERROR_MSG_HAND_NOTE_NOT_MATCH = "HAND_NOTE_NOT_MATCH";
    public static final String ERROR_MSG_ERROR_ID_EXPIRD = "ID_EXPIRD";

    public static final String KYC_PASS_FORBID_COUNTRY_MESSAGE = "KYC_PASS_FORBID_COUNTRY_MESSAGE";

    /** jumio审核失败原因 */
    public static final Map<String,String> REASON_CN = new HashMap<>();
    public static final Map<String,String> REASON_EN = new HashMap<>();

    public static String getReasonCn(String key){
        return REASON_CN.get(key);
    }
    public static String getReasonEn(String key){
        return REASON_EN.get(key);
    }

    static {
        REASON_CN.put("SELFIE_CROPPED_FROM_ID", "自拍照不能从证件中裁剪");
        REASON_CN.put("ENTIRE_ID_USED_AS_SELFIE", "证件不能用于自拍照");
        REASON_CN.put("MULTIPLE_PEOPLE", "图像中不能出现多人");
        REASON_CN.put("SELFIE_IS_SCREEN_PAPER_VIDEO", "自拍照不能使用纸张和视频的截图");
        REASON_CN.put("SELFIE_MANIPULATED", "自拍照不能修图过多");
        REASON_CN.put("AGE_DIFFERENCE_TOO_BIG", "外观不能变化太大");
        REASON_CN.put("NO_FACE_PRESENT", "自拍照不能没有面部");
        REASON_CN.put("FACE_NOT_FULLY_VISIBLE", "面部遮挡不能过多");
        REASON_CN.put("BAD_QUALITY", "照片不能太模糊");
        REASON_CN.put("BLACK_AND_WHITE", "照片不能是黑白的");
        REASON_CN.put("MANIPULATED_DOCUMENT", "不能上传篡改的照片");
        REASON_CN.put("FRAUDSTER", "不能上传篡改的照片");
        REASON_CN.put("FAKE", "不能上传篡改的照片");
        REASON_CN.put("PHOTO_MISMATCH", "持有证件的人与证件照不匹配");
        REASON_CN.put("PUNCHED_DOCUMENT", "失效的证件");
        REASON_CN.put("PHOTOCOPY_BLACK_WHITE", "不能使用复印件");
        REASON_CN.put("PHOTOCOPY_COLOR", "不能使用复印件");
        REASON_CN.put("DIGITAL_COPY", "不能使用电子设备上的照片");
        REASON_CN.put("NOT_READABLE_DOCUMENT", "无法读取您的证件，请上传清晰、完整、无遮挡的照片");
        REASON_CN.put("NO_DOCUMENT", "请上传有效的证件");
        REASON_CN.put("SAMPLE_DOCUMENT", "不能上传无效的样本");
        REASON_CN.put("MISSING_BACK", "证件背面缺失");
        REASON_CN.put("WRONG_DOCUMENT_PAGE", "证件无有效的验证信息");
        REASON_CN.put("MISSING_SIGNATURE", "证件缺失签名");
        REASON_CN.put("CAMERA_BLACK_WHITE", "黑白的图像不能用于验证");
        REASON_CN.put("MANUAL_REJECTION", "验证失败，请重试");
        REASON_CN.put("SIMILARITY_NOT_MATCH", "自拍照与证件不相符");
        REASON_CN.put(ERROR_MSG_HAND_NOTE_NOT_MATCH, "手持个人签字照不符合要求");
        REASON_CN.put("DENIED_UNSUPPORTED_ID_TYPE", "无效的证件类型");
        REASON_CN.put(ERROR_MSG_ERROR_ID_EXPIRD, "证件已过期");
        REASON_CN.put("DENIED_UNSUPPORTED_ID_COUNTRY", "国家不在支持列表");
        REASON_CN.put(ERROR_MSG_ID_NUMBER, "证件号码已被使用");
        REASON_CN.put(STATUS_DENIED_FRAUD, "请不要上传篡改的证件");
        REASON_CN.put(STATUS_ERROR_NOT_READABLE_ID, "请上传有效的证件");

        REASON_EN.put("SELFIE_CROPPED_FROM_ID", "The selfie picture provided is cropped from ID document");
        REASON_EN.put("ENTIRE_ID_USED_AS_SELFIE", "The entire ID document is used as selfie picture");
        REASON_EN.put("MULTIPLE_PEOPLE", "There are multiple people in the provided selfie picture");
        REASON_EN.put("SELFIE_IS_SCREEN_PAPER_VIDEO", "The selfie picture provided is taken from either a paper or a screenshot from picture or video");
        REASON_EN.put("SELFIE_MANIPULATED", "The selfie picture is manipulated/photshoped/modified");
        REASON_EN.put("AGE_DIFFERENCE_TOO_BIG", "We are unable to match the selfie with ID picture due to the age difference is too big");
        REASON_EN.put("NO_FACE_PRESENT", "There is no face present in the provided selfie picture");
        REASON_EN.put("FACE_NOT_FULLY_VISIBLE", "The face is not fully visible in the provided selfie for verification");
        REASON_EN.put("BAD_QUALITY", "The provided selfie quality is not good enough for verification");
        REASON_EN.put("BLACK_AND_WHITE", "The provided selfie is a black and white picture which is not accepted for verification");
        REASON_EN.put("MANIPULATED_DOCUMENT", "The part of the document has been tampered");
        REASON_EN.put("FRAUDSTER", "The part of the document has been tampered");
        REASON_EN.put("FAKE", "The part of the document has been tampered");
        REASON_EN.put("PHOTO_MISMATCH", "The person holding the document is not matching with the photo");
        REASON_EN.put("MRZ_CHECK_FAILED", "The MRZ check digit calculations failed");
        REASON_EN.put("PUNCHED_DOCUMENT", "Invalid documents");
        REASON_EN.put("MISMATCH_PRINTED_BARCODE_DATA", "The document doesn't match the bar code content");
        REASON_EN.put("PHOTOCOPY_BLACK_WHITE", "Can't use a photocopy");
        REASON_EN.put("PHOTOCOPY_COLOR", "Can't use a photocopy");
        REASON_EN.put("DIGITAL_COPY", "The document displays a photocopy of the ID on an electronic device");
        REASON_EN.put("NOT_READABLE_DOCUMENT", "Unable to read your documents. Please upload clear, complete and unblocked photos");
        REASON_EN.put("NO_DOCUMENT", "The document provided was not an actual ID or cannot be used for verification");
        REASON_EN.put("SAMPLE_DOCUMENT", "The document is a sample which cannot be used for verification");
        REASON_EN.put("MISSING_BACK", "The Back of the document is missing and mandatory for verification");
        REASON_EN.put("WRONG_DOCUMENT_PAGE", "The document does not show the photo and data page which is mandatory for verification");
        REASON_EN.put("MISSING_SIGNATURE", "The document is missing the signature");
        REASON_EN.put("CAMERA_BLACK_WHITE", "The Camera delivered a black and white image which is not accepted for verification");
        REASON_EN.put("DIFFERENT_PERSONS_SHOWN", "More than one document was found in the provided image  / Different persons shown is when there is a different individual holding the document that is represented in the image of the document");
        REASON_EN.put("MANUAL_REJECTION", "Verify failure, please try again");
        REASON_EN.put("SIMILARITY_NOT_MATCH", "The selfie is not consistent with documents");
        REASON_EN.put(ERROR_MSG_HAND_NOTE_NOT_MATCH, "The personal photograph with signature does not meet requirements");
        REASON_EN.put("DENIED_UNSUPPORTED_ID_TYPE", "Invalid ID type");
        REASON_EN.put(ERROR_MSG_ERROR_ID_EXPIRD, "The document has expired");
        REASON_EN.put("DENIED_UNSUPPORTED_ID_COUNTRY", "Unsupported Country");
        REASON_EN.put(ERROR_MSG_ID_NUMBER, "Your photo ID has been authenticated");
        REASON_EN.put(STATUS_DENIED_FRAUD, "Please do not upload manipulated document");
        REASON_EN.put(STATUS_ERROR_NOT_READABLE_ID, "Please upload a valid ID");
    }
}
