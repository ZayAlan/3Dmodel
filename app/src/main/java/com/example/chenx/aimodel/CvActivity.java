package com.example.chenx.aimodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.chenx.aimodel.file.FileService;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import static org.opencv.calib3d.Calib3d.RANSAC;
import static org.opencv.calib3d.Calib3d.Rodrigues;
import static org.opencv.calib3d.Calib3d.findEssentialMat;
import static org.opencv.calib3d.Calib3d.recoverPose;
import static org.opencv.calib3d.Calib3d.solvePnPRansac;
import static org.opencv.calib3d.Calib3d.triangulatePoints;

public class CvActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

//    private native void getFeatures(List<String> imagePaths, List<MatOfKeyPoint> allKeyPoints, List<Mat> allDescriptors);
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    //处理工作
                    dealMat();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    private static String TAG = "CameraActivity";
//    private ImageView originalImage;
//    Mat srcImage,src1,src2;
    private List<Mat> mats = new ArrayList<>();                             //照片mat
    private List<MatOfKeyPoint> allKeyPoints = new ArrayList<>();           //所有关键点保存
    private List<Mat> allDescriptors = new ArrayList<>();                   //所有描述子保存
    private List<List<DMatch> > allMatches = new ArrayList<>();             //特征点
    private MatOfPoint3f structure = new MatOfPoint3f();                    //三维点
    private List<List<Integer>> correspondStructIdx = new ArrayList<>();   // //保存第i副图像中第j个特征点对应的structure中点的索引
    private List<Mat> rotations = new ArrayList<>();                        //R
    private List<Mat> motions = new ArrayList<>();                          //T
    private Mat K;                                                          //相机内参矩阵

    //求相机内参矩阵的参数
    public static float zoom;
    public static float pictureHeight;
    public static float pictureWidth;
    public static float sensorSizeHeight;
    public static float sensorSizeWidth;

    private ImageView testImageView;

    @SuppressLint("StaticFieldLeak")
    class BackgroundTask extends AsyncTask<Void, Integer, Bitmap>{
        Context context;
        BackgroundTask(Context context){
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            //初始化一些变量
            FeatureDetector detector;
            DescriptorExtractor descriptorExtractor;
            detector = FeatureDetector.create(FeatureDetector.SIFT);
            descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);

            //提取所有图片的特征
            for (Mat tempMat: mats){
                MatOfKeyPoint keyPoint = new MatOfKeyPoint();
                Mat descripter = new Mat();

                detector.detect(tempMat,keyPoint);     //检测关键点
                descriptorExtractor.compute(tempMat,keyPoint,descripter);       //计算描述子

                //特征点过少排除该图像
                if(keyPoint.toArray().length<=10) continue;

                allKeyPoints.add(keyPoint);
                allDescriptors.add(descripter);

                //color缺失
            }

            //对所有图片进行顺次的特征匹配
            for (int i = 0; i<allDescriptors.size()-1;i++){
                //n个图像，两两配对
                DescriptorMatcher descriptorMatcher;  //匹配器
                //匹配的特征点集
                List<MatOfDMatch> mKnnMatches = new LinkedList<>();
                descriptorMatcher= DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

                //使用KNN-matching算法在相邻图像的特征描述子中寻找最佳匹配
                //令K=2，则每个match得到两个最接近的descriptor
                descriptorMatcher.knnMatch(allDescriptors.get(i),allDescriptors.get(i+1), mKnnMatches,2);

                //找出满足Ratio Test的最小匹配的距离
                Float min_dist =  Float.MAX_VALUE;

                for (MatOfDMatch mKnnMatch : mKnnMatches) {
                    DMatch[] dmatcharray = mKnnMatch.toArray();
                    DMatch m1 = dmatcharray[0];
                    DMatch m2 = dmatcharray[1];

                    //Ratio Test, 设置既定值为0.6
                    if (m1.distance <= m2.distance * 0.6) {
                        float dist = m1.distance;
                        if (dist < min_dist) {
                            min_dist = dist;
                        }
                    }
                }

                List<DMatch> matches = new ArrayList<>();
                for (MatOfDMatch match : mKnnMatches) {
                    DMatch[] dmatcharray = match.toArray();
                    DMatch m1 = dmatcharray[0];
                    DMatch m2 = dmatcharray[1];
                    //保存满足Ratio Test的点和匹配距离适合的点
                    if (m1.distance <= m2.distance * 0.6 || m1.distance <= 0.5 * Math.max(min_dist, 10.0f)) {
                        matches.add(m1);
                    }
                }
                allMatches.add(matches);
            }

            //计算内参矩阵K
            double u0 = pictureWidth/2;
            double v0 = pictureHeight/2;
            double dx = sensorSizeWidth/pictureWidth;
            double dy = sensorSizeHeight/pictureHeight;
            double fx = zoom/dx;
            double fy = zoom/dy;
            K = new Mat(3, 3, CvType.CV_64FC1);
            initialK(K.getNativeObjAddr(),fx, u0, fy, v0);

            //初始化structure

            //计算头两副图像的变换矩阵
            MatOfPoint2f p1,p2;
            Mat R = new Mat();
            Mat T = new Mat();      //旋转矩阵和平移向量
            Mat mask = new Mat();   //mask中大于零的点代表匹配点，等于零代表失配点
            p1 = new MatOfPoint2f();
            p2 = new MatOfPoint2f();

            //getMatchedPoints: p1, p2
            List<Point> savePoint1 = new ArrayList<>();
            List<Point> savePoint2 = new ArrayList<>();
            List<DMatch> matchesZero = allMatches.get(0);
            for (DMatch match : matchesZero) {
                KeyPoint tempKey1 = allKeyPoints.get(0).toArray()[match.queryIdx];
                KeyPoint tempKey2 = allKeyPoints.get(1).toArray()[match.trainIdx];
                savePoint1.add(tempKey1.pt);
                savePoint2.add(tempKey2.pt);
            }
            p1.fromList(savePoint1);
            p2.fromList(savePoint2);

            //find Transform
            //根据内参矩阵获取相机的焦距和光心坐标（主点坐标）
            double focal_length = 0.5*(fx + fy);
            Point principle_point = new Point(u0, v0);

            //根据匹配点求取本征矩阵E，使用RANSAC，进一步排除失配点
            Mat E = findEssentialMat(p1, p2, focal_length, principle_point, RANSAC, 0.999, 1.0, mask);

            //分解本征矩阵，获取相对变换R、T
            recoverPose(E, p1, p2, R, T, focal_length, principle_point, mask);

            //对头两幅图片进行三维重建
            MatOfPoint2f p1_copy = p1;
            p1 = new MatOfPoint2f();
            savePoint1.clear();
            for (int i = 0; i<mask.rows();i++){
                int temp = getMaskNum(mask.getNativeObjAddr(),i);
                if (temp > 0){
                    savePoint1.add(p1_copy.toArray()[i]);
                }
            }
            p1.fromList(savePoint1);

            MatOfPoint2f p2_copy = p2;
            p2 = new MatOfPoint2f();
            savePoint2.clear();
            for (int i = 0; i<mask.rows();i++){
                int temp = getMaskNum(mask.getNativeObjAddr(),i);
                if (temp > 0){
                    savePoint2.add(p2_copy.toArray()[i]);
                }
            }
            p2.fromList(savePoint2);

            Mat R0 = Mat.eye(3,3,CvType.CV_64FC1);
            Mat T0 = Mat.zeros(3,1,CvType.CV_64FC1);

            //reconstruct重建
            //两个相机的投影矩阵[R T]，triangulatePoints只支持float型
            Mat proj1 = new Mat(3, 4, CvType.CV_32FC1);
            Mat proj2 = new Mat(3, 4, CvType.CV_32FC1);
            dealRandT(R0.getNativeObjAddr(),T0.getNativeObjAddr(),proj1.getNativeObjAddr());
            dealRandT(R.getNativeObjAddr(),T.getNativeObjAddr(),proj2.getNativeObjAddr());
            Mat fk = new Mat();
            K.convertTo(fk, CvType.CV_32FC1);
            doMultiplyMat(fk.getNativeObjAddr(),proj1.getNativeObjAddr(),proj2.getNativeObjAddr());

            //三角重建
            Mat s = new Mat();
            triangulatePoints(proj1,proj2,p1,p2,s);

            List<Point3> point3List = new ArrayList<>();
            float col1,col2,col3;
            for (int i = 0; i<s.cols();i++){
                //齐次坐标，需要除以最后一个元素才是真正的坐标值
                col1 = getColDetail(s.getNativeObjAddr(), i,0);
                col2 = getColDetail(s.getNativeObjAddr(), i,1);
                col3 = getColDetail(s.getNativeObjAddr(), i,2);
                point3List.add(new Point3(col1,col2,col3));
            }
            structure.fromList(point3List);

            //保存变换矩阵
            rotations.add(R0);
            rotations.add(R);
            motions.add(T0);
            motions.add(T);

            //将correspondStructIdx的大小初始化为与allKeyPoints完全一致,且默认值置-1
            for (MatOfKeyPoint keyPoint : allKeyPoints){
                int tempSize = keyPoint.toArray().length;
                List<Integer> layerNumbers = new ArrayList<>();
                for (int i = 0; i<tempSize;i++){
                    layerNumbers.add(-1);
                }
                correspondStructIdx.add(layerNumbers);
            }

            //填写头两幅图像的结构索引
            int idx = 0;
            List<DMatch> matches = allMatches.get(0);
            List<Integer> zeroVTemp = correspondStructIdx.get(0);
            List<Integer> oneVTemp = correspondStructIdx.get(1);
            for (int i = 0; i < matches.size(); ++i) {
                int temp = getMaskNum(mask.getNativeObjAddr(), i);
                if (temp == 0) continue;
                zeroVTemp.set(matches.get(i).queryIdx, idx);
                oneVTemp.set(matches.get(i).trainIdx, idx);
                ++idx;
            }
            correspondStructIdx.set(0,zeroVTemp);
            correspondStructIdx.set(1,oneVTemp);

            //增量方式重建剩余的图像
            for (int i = 1; i < allMatches.size(); ++i) {
                MatOfPoint3f object_points = new MatOfPoint3f();
                MatOfPoint2f image_points = new MatOfPoint2f();
                Mat r = new Mat();
                Mat Rn = new Mat();
                Mat Tn = new Mat();

                List<Point3> saveObjectPoints = new ArrayList<>();
                List<Point> saveImagePoints = new ArrayList<>();


                //获取第i幅图像中匹配点对应的三维点，以及在第i+1幅图像中对应的像素点
                for (DMatch nMatch : allMatches.get(i)){
                    int query_Idx = nMatch.queryIdx;
                    int train_Idx = nMatch.trainIdx;
                    int struct_Idx = correspondStructIdx.get(i).get(query_Idx);
                    if (struct_Idx<0) continue;
                    saveObjectPoints.add(structure.toArray()[struct_Idx]);
                    saveImagePoints.add(allKeyPoints.get(i+1).toArray()[train_Idx].pt);
                }
                object_points.fromList(saveObjectPoints);
                image_points.fromList(saveImagePoints);

                //求解变换矩阵
                MatOfDouble diff = new MatOfDouble();
                prepareDiffMat(diff.getNativeObjAddr());
                solvePnPRansac(object_points,image_points, K, diff,r,Tn);

                //将旋转向量转换为旋转矩阵
                Rodrigues(r,Rn);

                //保存变换矩阵
                rotations.add(Rn);
                motions.add(Tn);

                MatOfPoint2f pn1 = new MatOfPoint2f();
                MatOfPoint2f pn2 = new MatOfPoint2f();
                List<Point> pn1List = new ArrayList<>();
                List<Point> pn2List = new ArrayList<>();
                for (DMatch dMatch: allMatches.get(i)){
                    KeyPoint tempKey1 = allKeyPoints.get(i).toArray()[dMatch.queryIdx];
                    KeyPoint tempKey2 = allKeyPoints.get(i+1).toArray()[dMatch.trainIdx];
                    pn1List.add(tempKey1.pt);
                    pn2List.add(tempKey2.pt);
                }
                pn1.fromList(pn1List);
                pn2.fromList(pn2List);

                //根据之前求得的R，T进行三维重建
                MatOfPoint3f nextStructure = new MatOfPoint3f();

                Mat projn1 = new Mat(3, 4, CvType.CV_32FC1);
                Mat projn2 = new Mat(3, 4, CvType.CV_32FC1);
                dealRandT(rotations.get(i).getNativeObjAddr(), motions.get(i).getNativeObjAddr(), projn1.getNativeObjAddr());
                dealRandT(Rn.getNativeObjAddr(), Tn.getNativeObjAddr(), projn2.getNativeObjAddr());
                Mat fkn = new Mat();
                K.convertTo(fkn,CvType.CV_32FC1);
                doMultiplyMat(fkn.getNativeObjAddr(),projn1.getNativeObjAddr(),projn2.getNativeObjAddr());

                //三角重建
                Mat sn = new Mat();
                triangulatePoints(projn1,projn2,pn1,pn2,sn);
                List<Point3> nextStructureList = new ArrayList<>();
                float scol1,scol2,scol3;
                for (int h = 0; h<sn.cols();h++){
                    //齐次坐标，需要除以最后一个元素才是真正的坐标值
                    scol1 = getColDetail(sn.getNativeObjAddr(), h,0);
                    scol2 = getColDetail(sn.getNativeObjAddr(), h,1);
                    scol3 = getColDetail(sn.getNativeObjAddr(), h,2);
                    nextStructureList.add(new Point3(scol1,scol2,scol3));
                }
                nextStructure.fromList(nextStructureList);

                //将新的重建结果与之前的融合
                for(DMatch match: allMatches.get(i)){
                    int query_idx = match.queryIdx;
                    int train_idx = match.trainIdx;
                    int struct_idx = correspondStructIdx.get(i).get(query_idx);
                    if (struct_idx >= 0){                 //若该点在空间中已经存在，则这对匹配点对应的空间点应该是同一个，索引要相同
                        correspondStructIdx.get(i+1).set(train_idx,struct_idx);
                        continue;
                    }

                    //将该点加入到结构中，且这对匹配点的空间点索引都为新加入的点的索引
                    point3List.add(nextStructure.toArray()[i]);
                    structure = new MatOfPoint3f();
                    structure.fromList(point3List);
                    correspondStructIdx.get(i).set(query_idx, structure.toArray().length-1);
                    correspondStructIdx.get(i+1).set(train_idx, structure.toArray().length-1);
                }
            }

            //rotations 旋转矩阵,
            // motions 平移矩阵
            // structure
            //保存建模文件
            FileService fileService = new FileService(getApplicationContext());
            String contentName = "aTestStructure";
            fileService.SaveToSDcard(contentName,"structureMatrix",structure);
//            fileService.SaveToSDcard(contentName, "rotationMatrixs",rotations);
//            fileService.SaveToSDcard(contentName,"motionMatrixs",motions);

            Bitmap testBitmap = Bitmap.createBitmap(mats.get(0).cols(),mats.get(0).rows(),Bitmap.Config.ARGB_8888);  //
            Utils.matToBitmap(mats.get(0),testBitmap);  //
            return testBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            testImageView.setImageBitmap(bitmap);
        }
    }


    private void dealMat() {
        String filePathString = Environment.getExternalStorageDirectory().toString() + File.separator + "aimodel";
        File fileAll = new File(filePathString);
        File[] files = fileAll.listFiles();

        //加速位图加载，建议其他使用处也进行这样处理
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        //加载所有图片
        for (File file : files) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            Mat tempMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap, tempMat);
            mats.add(tempMat);
        }

        BackgroundTask task = new BackgroundTask(this);
        task.execute();
    }

    private native void prepareDiffMat(long diff);
    private native float getColDetail(long col,int i,int j);
    private native void initialK(long k,double fx, double u0,double fy, double v0);
    private native void doMultiplyMat(long k,long projectMat1,long projectMat2);
    private native void dealRandT(long R, long T, long projectMat);
    private native int getMaskNum(long mask, int i);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv);
        testImageView = findViewById(R.id.testImageView);
//        testText = findViewById(R.id.testText);
        Button button = findViewById(R.id.openGLDeal);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CvActivity.this, GLViewActivity.class);
                GLViewActivity.originalStructure = structure;
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
