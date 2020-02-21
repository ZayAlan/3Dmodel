#include <jni.h>
#include <string>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/xfeatures2d/nonfree.hpp>
#include <opencv2/imgcodecs/imgcodecs.hpp>
#include <opencv2/calib3d/calib3d.hpp>

using namespace cv;

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_chenx_aimodel_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_chenx_aimodel_CvActivity_dealRandT(JNIEnv *env, jobject instance, jlong R, jlong T,
                                                    jlong projectMat) {

    // TODO
    cv::Mat R0 = *(cv::Mat*) R;
    cv::Mat T0 = *(cv::Mat*) T;
    cv::Mat proj1 = *(cv::Mat*) projectMat;
    R0.convertTo(proj1(Range(0, 3), Range(0, 3)), CV_32FC1);
    T0.convertTo(proj1.col(3), CV_32FC1);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_chenx_aimodel_CvActivity_doMultiplyMat(JNIEnv *env, jobject instance,jlong k,
                                                        jlong projectMat1, jlong projectMat2) {

    // TODO
    cv::Mat K = *(cv::Mat*) k;
    cv::Mat projMat1 = *(cv::Mat*) projectMat1;
    cv::Mat projMat2 = *(cv::Mat*) projectMat2;
    projMat1 = K * projMat1;
    projMat2 = K * projMat2;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_chenx_aimodel_CvActivity_initialK(JNIEnv *env, jobject instance, jlong k,
                                                   jdouble fx, jdouble u0, jdouble fy, jdouble v0) {

    cv::Mat K = *(cv::Mat*) k;
    Mat temp(Matx33d(
            fx, 0 , u0,
            0, fy , v0,
            0, 0  , 1));
    temp.convertTo(K,CV_64FC1);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_chenx_aimodel_CvActivity_getColDetail(JNIEnv *env, jobject instance, jlong s, jint i, jint j) {

    cv::Mat sMat = *(cv::Mat*)s;
    cv::Mat_<float> colMat = sMat.col(i);
    colMat /= colMat(3);
    jfloat colfloat = colMat(j);
    return colfloat;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_chenx_aimodel_CvActivity_prepareDiffMat(JNIEnv *env, jobject instance,
                                                         jlong diff) {

    cv::Mat diffMat = *(cv::Mat*) diff;
    Mat temp(Matx41d(
            0,
            0,
            0,
            0));
    temp.convertTo(diffMat,CV_64FC1);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_chenx_aimodel_CvActivity_getMaskNum(JNIEnv *env, jobject instance, jlong mask,
                                                     jint i){
    cv::Mat CMask = *(cv::Mat*) mask;
    jint num = CMask.at<uchar>(i);
    return num;
}