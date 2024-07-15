# OpenCV入门指南

- Canny边缘检测
- 图像的二值化
- 轮廓检测
- 常用函数

## Canny边缘检测

图像的边缘检测的原理是检测出图像中所有灰度值变化较大的点，而且这些点连接起来就构成了若干线条，这些线条就可以称为图像的边缘。

Canny边缘检测算子是John F. Canny于 1986 年开发出来的一个多级边缘检测算法。Canny 边缘检测的数学原理和算法实现这里就不再了，有兴趣的读者可以查阅专业书籍，本文主要介绍如何在OpenCV中对图像进行Canny 边缘检测，下面就来看看这个函数的原型。
    
### 一、主要函数 

##### 1.1 cvCanny

函数功能：采用Canny方法对图像进行边缘检测

函数原型：

```c++
void cvCanny(
  const CvArr* image,
  CvArr* edges,
  double threshold1,double threshold2,
  int aperture_size=3,
  boolean L2_gradient
);
```

函数说明：

- image 输入单通道图像（可以是彩色图像）对于多通道的图像可以用cvCvtColor()修改；
    使用`cvCvtColor()`：将彩色图像转换为灰度图像。
- edges 输出的边缘图像，也是单通道的，但是是黑白的；
- 第三个参数和第四个参数表示阈值，这二个阈值当中：
    小的阈值用来控制边缘连接；
    大的阈值用来控制强边缘的初始分割，即如果一个像素的梯度大于上限值，则被认为是边缘像素，如果小于下限阈值，则被抛弃。
        如果该点的梯度在两者之间则当这个点与高于上限值的像素点连接时我们才保留，否则删除。
- 可选参数aperture_Size表示Sobel算子大小，默认为3即表示一个3*3的矩阵。Sobel 算子与高斯拉普拉斯算子都是常用的边缘算子，详细的数学原理可以查阅专业书籍。
- 可选参数`L2_gradient`是一个布尔值，
    如果为真，则使用更精确的L2范数进行计算，否则使用L1范数
    
为了更好的使用`cvCanny()`函数，下面再介绍二个实用的函数，这二个函数对后面的程序实现非常有帮助。

##### 1.2 cvCreateTrackbar

函数功能：创建trackbar并添加到指定窗口

函数原型：

```c++
intcvCreateTrackbar(
  const char* trackbar_name,
  const char* window_name,
  int* value,
  int count,
  CvTrackbarCallback on_change
);
```

函数说明：

- 第一个参数表示该trackbar的名称。
- 第二个参数表示窗口名称，该trackbar将显示在这个窗口内。
- 第三个参数表示创建时滑块的位置。
- 第四个参数表示滑块位置的最大值，最小值固定为0。
- 第五个参数表示回调函数。当滑块位置有变化时，系统会调用该回调函数。

注：被创建的trackbar默认显示在指定窗口的顶端，可以通过函数cvGetTrackbarPos()来获取trackbar显示的位置信息，以及通过函数cvSetTrackbarPos()来重新设置trackbar的显示位置。

##### 1.3、CvTrackbarCallback

函数功能：cvCreateTrackbar()函数所使用的回调函数

函数定义：

```c++
typedef void (CV_CDECL *CvTrackbarCallback)(int pos)
```

函数说明：

当trackbar位置被改变的时，系统会调用这个回调函数，并将参数pos设置为表示trackbar位置的数值。

## 二、示例程序代码

下面就给出在OpenCV中使用Canny边缘检测的程序代码：

```c++
//图像的Canny边缘检测
#include <opencv2/opencv.hpp>
using namespace std;
#pragma comment(linker, "/subsystem:\"windows\" /entry:\"mainCRTStartup\"")
IplImage *g_pSrcImage, *g_pCannyImg;
const char *pstrWindowsCannyTitle = "边缘检测图(http://blog.csdn.net/MoreWindows)";
//cvCreateTrackbar的回调函数
void on_trackbar(int threshold)
{
	//canny边缘检测
	cvCanny(g_pSrcImage, g_pCannyImg, threshold, threshold * 3, 3);
	cvShowImage(pstrWindowsCannyTitle, g_pCannyImg);
}
int main()
{
	const char *pstrImageName = "001.jpg";
	const char *pstrWindowsSrcTitle = "原图(http://blog.csdn.net/MoreWindows)";
	const char *pstrWindowsToolBar = "Threshold";

	//从文件中载入图像的灰度图CV_LOAD_IMAGE_GRAYSCALE - 灰度图
	g_pSrcImage = cvLoadImage(pstrImageName, CV_LOAD_IMAGE_GRAYSCALE);
	g_pCannyImg = cvCreateImage(cvGetSize(g_pSrcImage), IPL_DEPTH_8U, 1);

	//创建窗口
	cvNamedWindow(pstrWindowsSrcTitle, CV_WINDOW_AUTOSIZE);
	cvNamedWindow(pstrWindowsCannyTitle, CV_WINDOW_AUTOSIZE);

//创建滑动条
	int nThresholdEdge = 1;
	cvCreateTrackbar(pstrWindowsToolBar, pstrWindowsCannyTitle, &nThresholdEdge, 100, on_trackbar);

	//在指定窗口中显示图像
	cvShowImage(pstrWindowsSrcTitle, g_pSrcImage);
	on_trackbar(1);

	//等待按键事件
	cvWaitKey();

	cvDestroyWindow(pstrWindowsSrcTitle);
	cvDestroyWindow(pstrWindowsCannyTitle);
	cvReleaseImage(&g_pSrcImage);
	cvReleaseImage(&g_pCannyImg);
	return 0;
}
```

运行效果如图所示：

![](../../../../../../../../../images/cvCanny.PNG)

本篇介绍了Canny边缘检测，这种方法能有效的找出图像中的所有边缘。后面将还有文章介绍在OpenCV中对图像进行轮廓检测。

在对图像进行轮廓检测前必须要先对图像进行二值化。

---

## 图像的二值化

在上一篇《【OpenCV入门指南】Canny边缘检测》中介绍了使用Canny算子对图像进行边缘检测。
与边缘检测相比，轮廓检测有时能更好的反映图像的内容。而要对图像进行轮廓检测，则必须要先对图像进行二值化，
图像的二值化就是将图像上的像素点的灰度值设置为0或255，这样将使整个图像呈现出明显的黑白效果。
在数字图像处理中，二值图像占有非常重要的地位，图像的二值化使图像中数据量大为减少，从而能凸显出目标的轮廓。

### 一、关键函数介绍

下面就介绍OpenCV中对图像进行二值化的关键函数 —— `cvThreshold()`。

`cvThreshold`是opencv库中的一个函数。

作用：函数 `cvThreshold` 对**单通道数组**应用固定阈值操作。
该函数的典型应用是对灰度图像进行阈值操作得到二值图像。或者是去掉噪声，例如过滤很小或很大像素值的图像点。
本函数支持的对图像取阈值的方法由 `threshold_type` 确定。

使用`cvThreshold()`;将**灰度图像**转换为**二值图像**（该函数**只适用于单通道图像**）

函数功能：对图像进行二值化

函数原型：

```c++
void cvThreshold(
  const CvArr* src,
  CvArr* dst,
  double threshold,
  double max_value,
  int threshold_type
); 
```

函数说明：

- src：原始数组 (单通道)
- dst：输出数组
- 第三个参数表示阈值
- 第四个参数表示最大值。
- 第五个参数表示运算方法。

在OpenCV的imgproc\types_c.h中可以找到运算方法的定义。

```c++
/* Threshold types */
enum
{
    CV_THRESH_BINARY      =0,  /* value = value > threshold ? max_value : 0       */
    CV_THRESH_BINARY_INV  =1,  /* value = value > threshold ? 0 : max_value       */
    CV_THRESH_TRUNC       =2,  /* value = value > threshold ? threshold : value   */
    CV_THRESH_TOZERO      =3,  /* value = value > threshold ? value : 0           */
    CV_THRESH_TOZERO_INV  =4,  /* value = value > threshold ? 0 : value           */
    CV_THRESH_MASK        =7,
    CV_THRESH_OTSU        =8  /* use Otsu algorithm to choose the optimal threshold value; combine the flag with one of the above CV_THRESH_* values */
};
```

注释已经写的很清楚了，因此不再用中文来表达了

### 二、示例程序代码

下面给出对图像进行二值化的完整的源代码：

```c++
//图像的二值化
#include <opencv2/opencv.hpp>
using namespace std;

#pragma comment(linker, "/subsystem:\"windows\" /entry:\"mainCRTStartup\"")

IplImage *g_pGrayImage = NULL;
IplImage *g_pBinaryImage = NULL;
const char *pstrWindowsBinaryTitle = "二值图(http://blog.csdn.net/MoreWindows)";

void on_trackbar(int pos)
{
	// 转为二值图
	cvThreshold(g_pGrayImage, g_pBinaryImage, pos, 255, CV_THRESH_BINARY);
	// 显示二值图
	cvShowImage(pstrWindowsBinaryTitle, g_pBinaryImage);
}

int main( int argc, char** argv )
{	
	const char *pstrWindowsSrcTitle = "原图(http://blog.csdn.net/MoreWindows)";
	const char *pstrWindowsToolBarName = "二值图阈值";

	// 从文件中加载原图
	IplImage *pSrcImage = cvLoadImage("002.jpg", CV_LOAD_IMAGE_UNCHANGED);

	// 转为灰度图
	g_pGrayImage =  cvCreateImage(cvGetSize(pSrcImage), IPL_DEPTH_8U, 1);
	cvCvtColor(pSrcImage, g_pGrayImage, CV_BGR2GRAY);

	// 创建二值图
	g_pBinaryImage = cvCreateImage(cvGetSize(g_pGrayImage), IPL_DEPTH_8U, 1);

	// 显示原图
	cvNamedWindow(pstrWindowsSrcTitle, CV_WINDOW_AUTOSIZE);
	cvShowImage(pstrWindowsSrcTitle, pSrcImage);
	// 创建二值图窗口
	cvNamedWindow(pstrWindowsBinaryTitle, CV_WINDOW_AUTOSIZE);

	// 滑动条	
	int nThreshold = 0;
	cvCreateTrackbar(pstrWindowsToolBarName, pstrWindowsBinaryTitle, &nThreshold, 254, on_trackbar);

	on_trackbar(1);

	cvWaitKey(0);

	cvDestroyWindow(pstrWindowsSrcTitle);
	cvDestroyWindow(pstrWindowsBinaryTitle);
	cvReleaseImage(&pSrcImage);
	cvReleaseImage(&g_pGrayImage);
	cvReleaseImage(&g_pBinaryImage);
	return 0;
}
```

运行结果如下所示，自己动手调试下阈值大小，看看生成的二值图有什么变化。

![](../../../../../../../../../images/cvThreshold.jpg)

OpenCV还有个`cvAdaptiveThreshold()`函数，这个函数会使用`Otsu`算法(大律法或最大类间方差法)（注1）来计算出一个全局阈值，然后根据这个阈值进行二值化。

当然直接使用上一篇《【OpenCV入门指南】Canny边缘检测》中的`cvCanny()`函数也可以对图像进行二值化（想到怎么传参数了吗？）。

注1．调用`cvThreshold()`时传入参数`CV_THRESH_OTSU`也是使用`Otsu`算法来自动生成一个阈值。

## 轮廓检测(上)

《【OpenCV入门指南】Canny边缘检测》中介绍了边缘检测，本篇介绍轮廓检测，轮廓检测的原理通俗的说就是掏空内部点，比如原图中有3*3的矩形点。那么就可以将中间的那一点去掉。

在OpenCV中使用轮廓检测是非常方便。直接使用cvFindContours函数就能完成对图像轮廓的检测。下面就来看看这个函数的用法。

### 一、关键函数

##### 1.1  cvFindContours

函数功能：对图像进行轮廓检测，这个函数将生成一条链表以保存检测出的各个轮廓信息，并传出指向这条链表表头的指针。

函数原型：

```c++
int cvFindContours(
  CvArr* image,
  CvMemStorage* storage,
  CvSeq** first_contour,
  int header_size=sizeof(CvContour),
  int mode=CV_RETR_LIST, 　　
  int method=CV_CHAIN_APPROX_SIMPLE,
  CvPoint offset=cvPoint(0,0)
);
```

函数说明：

- 第一个参数表示输入图像，必须为一个8位的二值图像。图像的二值化请参见《【OpenCV入门指南】图像的二值化》。
    从一个灰度图像得到二值图像的函数有：`cvThreshold`，`cvAdaptiveThreshold`和`cvCanny`
- 第二参数表示存储轮廓的容器。为`CvMemStorage`类型，定义在OpenCV的\core\types_c.h中
- 第三个参数为输出参数，这个参数将指向用来存储轮廓信息的链表表头
- 第四个参数表示存储轮廓链表的表头大小，当第六个参数传入`CV_CHAIN_CODE`时，要设置成sizeof(CvChain)，其它情况统一设置成sizeof(CvContour)。
- 第五个参数为轮廓检测的模式，有如下取值：
    - CV_RETR_EXTERNAL：只提取最外层的轮廓，查找外边缘，各边缘以指针h_next相连；
    - CV_RETR_LIST：提取所有的轮廓，并将其保存到一条链表当中。
        检测的轮廓不建立等级关系。
        查找所有边缘（包含内部空洞），各边缘以指针h_next相连；
    - CV_RETR_CCOMP：提取所有的轮廓，并且将其组织为两层的 hierarchy：顶层是各部分的外部边界，次层为洞的内层边界。
    - CV_RETR_TREE：提取所有轮廓，并且重构嵌套轮廓的全部 hierarchy
    
    ![](../../../../../../../../../images/cvFindContours.bmp)
    
- 第六个参数用来表示轮廓边缘的近似方法的，（除了CV_RETR_RUNS使用内置的近似，其他模式均使用此设定的近似算法）。可取值如下：
    - CV_CHAIN_CODE：以Freeman链码的方式输出轮廓，所有其他方法输出多边形（顶点的序列）。
    - CV_CHAIN_APPROX_NONE：将所有的连码点，转换成点。
    - CV_CHAIN_APPROX_SIMPLE：压缩水平方向，垂直方向，对角线方向的元素，只保留该方向的终点坐标，例如一个矩形轮廓只需4个点来保存轮廓信息
    - CV_CHAIN_APPROX_TC89_L1，CV_CHAIN_APPROX_TC89_KCOS：使用the flavors of Teh-Chin chain近似算法的一种。
    - CV_LINK_RUNS：通过连接水平段的1，使用完全不同的边缘提取算法。使用CV_RETR_LIST检索模式能使用此方法。
    
- 第七个参数表示偏移量，比如你要从图像的（100, 0）开始进行轮廓检测，那么就传入（100, 0）。

使用cvFindContours函数能检测出图像的轮廓，将轮廓绘制出来则需要另一函数 —— `cvDrawContours`来配合了。

下面介绍cvDrawContours函数。

##### 1.2  cvDrawContours

函数功能：在图像上绘制外部和内部轮廓

函数原型：

```c++
void cvDrawContours(
  CvArr *img,
  CvSeq* contour,
  CvScalar external_color,
  CvScalar hole_color,
  int max_level,
  int thickness=1,
  int line_type=8,
  CvPoint offset=cvPoint(0,0)
); 
```

函数说明：

- 第一个参数表示输入图像，函数将在这张图像上绘制轮廓。
- 第二个参数表示指向轮廓链表的指针。
- 第三个参数和第四个参数表示颜色，绘制时会根据轮廓的层次来交替使用这二种颜色。
- 第五个参数表示绘制轮廓的最大层数，
    如果是0，只绘制contour；
    如果是1，追加绘制和contour同层的所有轮廓；
    如果是2，追加绘制比contour低一层的轮廓，以此类推；
    如果值是负值，则函数并不绘制contour后的轮廓，但是将画出其子轮廓，一直到abs(max_level) - 1层。
- 第六个参数表示轮廓线的宽度，如果为`CV_FILLED`则会填充轮廓内部。
- 第七个参数表示轮廓线的类型。
- 第八个参数表示偏移量，如果传入（10，20），那绘制将从图像的（10，20）处开始。

### 二、示例程序代码

下面用一个非常简单的例子展示如何使用轮廓检测。

```c++
//图像的轮廓检测上
#include <opencv2/opencv.hpp>
using namespace std;
#pragma comment(linker, "/subsystem:\"windows\" /entry:\"mainCRTStartup\"")
int main( int argc, char** argv )
{	
	const char *pstrWindowsSrcTitle = "原图(http://blog.csdn.net/MoreWindows)";
	const char *pstrWindowsOutLineTitle = "轮廓图(http://blog.csdn.net/MoreWindows)";
	
	const int IMAGE_WIDTH = 400;
	const int IMAGE_HEIGHT = 200;

	// 创建图像
	IplImage *pSrcImage = cvCreateImage(cvSize(IMAGE_WIDTH, IMAGE_HEIGHT), IPL_DEPTH_8U, 3);
	// 填充成白色
	cvRectangle(pSrcImage, cvPoint(0, 0), cvPoint(pSrcImage->width, pSrcImage->height), CV_RGB(255, 255, 255), CV_FILLED);
	// 画圆
	CvPoint ptCircleCenter = cvPoint(IMAGE_WIDTH / 4, IMAGE_HEIGHT / 2);
	int nRadius = 80;
	cvCircle(pSrcImage, ptCircleCenter, nRadius, CV_RGB(255, 255, 0), CV_FILLED);
	ptCircleCenter = cvPoint(IMAGE_WIDTH / 4, IMAGE_HEIGHT / 2);
	nRadius = 30;
	cvCircle(pSrcImage, ptCircleCenter, nRadius, CV_RGB(255, 255, 255), CV_FILLED);
	// 画矩形
	CvPoint ptLeftTop = cvPoint(IMAGE_WIDTH / 2 + 20, 20);
	CvPoint ptRightBottom = cvPoint(IMAGE_WIDTH - 20, IMAGE_HEIGHT - 20);
	cvRectangle(pSrcImage, ptLeftTop, ptRightBottom, CV_RGB(0, 255, 255), CV_FILLED);
	ptLeftTop = cvPoint(IMAGE_WIDTH / 2 + 60, 40);
	ptRightBottom = cvPoint(IMAGE_WIDTH - 60, IMAGE_HEIGHT - 40);
	cvRectangle(pSrcImage, ptLeftTop, ptRightBottom, CV_RGB(255, 255, 255), CV_FILLED);
	// 显示原图
	cvNamedWindow(pstrWindowsSrcTitle, CV_WINDOW_AUTOSIZE);
	cvShowImage(pstrWindowsSrcTitle, pSrcImage);


	// 转为灰度图
	IplImage *pGrayImage =  cvCreateImage(cvGetSize(pSrcImage), IPL_DEPTH_8U, 1);
	cvCvtColor(pSrcImage, pGrayImage, CV_BGR2GRAY);
	// 转为二值图
	IplImage *pBinaryImage = cvCreateImage(cvGetSize(pGrayImage), IPL_DEPTH_8U, 1);
	cvThreshold(pGrayImage, pBinaryImage, 250, 255, CV_THRESH_BINARY);


	// 检索轮廓并返回检测到的轮廓的个数
	CvMemStorage *pcvMStorage = cvCreateMemStorage();
	CvSeq *pcvSeq = NULL;
	cvFindContours(pBinaryImage, pcvMStorage, &pcvSeq, sizeof(CvContour), CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
	
	// 画轮廓图
	IplImage *pOutlineImage = cvCreateImage(cvGetSize(pSrcImage), IPL_DEPTH_8U, 3);
	int nLevels = 5;
	// 填充成白色
	cvRectangle(pOutlineImage, cvPoint(0, 0), cvPoint(pOutlineImage->width, pOutlineImage->height), CV_RGB(255, 255, 255), CV_FILLED);
	cvDrawContours(pOutlineImage, pcvSeq, CV_RGB(255,0,0), CV_RGB(0,255,0), nLevels, 2);
	// 显示轮廓图
	cvNamedWindow(pstrWindowsOutLineTitle, CV_WINDOW_AUTOSIZE);
	cvShowImage(pstrWindowsOutLineTitle, pOutlineImage);


	cvWaitKey(0);

	cvReleaseMemStorage(&pcvMStorage);

	cvDestroyWindow(pstrWindowsSrcTitle);
	cvDestroyWindow(pstrWindowsOutLineTitle);
	cvReleaseImage(&pSrcImage);
	cvReleaseImage(&pGrayImage);
	cvReleaseImage(&pBinaryImage);
	cvReleaseImage(&pOutlineImage);
	return 0;
}
```

运行结果如下图所示：

![](../../../../../../../../../images/cvFindContours.PNG)

由图可以看出，轮廓线已经按层次交替的绘制成功了，读者可以修改程序中的cvDrawContours中的nLevels参数，看看图形会有什么变化。

下一篇《【OpenCV入门指南】轮廓检测下》将对一个复杂的图像进行轮廓检测，以便大家更好的观察出轮廓检测的特点。

## 常用函数

### 1. arcLength函数

函数功能：

主要是计算图像轮廓的周长

函数原型：

```c++
double arcLength(InputArray curve, bool closed)
```

参数详解：

- InputArray curve：表示图像的轮廓
- bool closed：表示轮廓是否封闭的

### 2. approxPolyDP函数

函数功能：

对图像轮廓点进行多边形拟合

函数原型：

```c++
void approxPolyDP(InputArray curve, OutputArray approxCurve, double epsilon, bool closed)
```

参数详解：

- InputArray curve:一般是由图像的轮廓点组成的点集
- OutputArray approxCurve：表示输出的多边形点集
- double epsilon：主要表示输出的精度，就是另个轮廓点之间最大距离数，5,6,7,8,,,,
- bool closed：表示输出的多边形是否封闭