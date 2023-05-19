


# 1. 一共五个文件 
   * OCRPredictorNative
     Native层 接口 一般不需要修改 对接底层
   * OcrResultModel
      Ocr识别结果实现类
   * Predictor
     Native 层的封装，可以方便调用此类方法
   * Utils
     工具类
   * MainActivity
     demo示例代码
     
# 2. 调用
    
        
        * sdk_ModelPath   
            模型参数配置 可选 models/ch_PP-OCRv3 性能更好  models/ch_PP-OCRv2 性能略差
        * sdk_labelPath 
            字典路径 labels/ppocr_keys_v1.txt
        * sdk_useOpencl
            默认为0 使用CPU推理  1 使用GPU推理
        * sdk_cpuThreadNum
            推理时线程数量，默认为4 线程数量越小越好
        * sdk_cpuPowerMode="LITE_POWER_HIGH";
            CPU调度策略    LITE_POWER_HIGH  LITE_POWER_LOW  LITE_POWER_FULL  LITE_POWER_NO_BIND LITE_POWER_RAND_HIGH LITE_POWER_RAND_LOW
            LITE_POWER_HIGH：指定使用高电平运行。当进入高电平模式时，处理器的主要时钟速度可能会提高。在此模式下，如果可用的总系统资源允许，处理器可能会以更高的频率运行。如果系统资源有限，则可能降低时钟速度以减少能耗。在LITE_POWER_HIGH模式下，处理器可以通过访问高速缓存中的数据和指令来提高性能。
            LITE_POWER_LOW：指定使用低电平运行。在低电平模式下，处理器的主要时钟速度将保持相对较低的水平。如果系统资源有限，处理器可能会以较低的频率运行，但能够节省能耗。在LITE_POWER_LOW模式下，处理器可以访问缓存中的数据和指令，但是频率相对较低。
            LITE_POWER_FULL：指定使用全电平运行。在全电平模式下，处理器将保持全速运行。这是默认的调度策略。在此模式下，处理器可以访问缓存中的数据和指令，但不能进行任何额外的计算。
            LITE_POWER_NO_BIND：指定处理器处于未绑定状态。在这种状态下，处理器的时钟速度相对较低，但可以访问高速缓存中的数据和指令。在LITE_POWER_NO_BIND模式下，处理器不能执行任何计算。
            LITE_POWER_RAND_HIGH：在此模式下，处理器运行在高随机性模式下。处理器的时钟速度可能会根据不同的系统事件或其他因素而变化。这种模式通常用于实现多种实验性或模拟性计算。
            LITE_POWER_RAND_LOW：在此模式下，处理器运行在低随机性模式下。处理器的时钟速度相对较低，但可以访问高速缓存中的数据和指令。在LITE_POWER_RAND_LOW模式下，处理器可以进行计算，但随机性相对较高。

        * sdk_detLongSize
            960 DB模型预处理时图像的长边长度，超过此长度resize到该值，短边进行等比例缩放，小于此长度不进行处理。   默认输入图像长宽大于960时，等比例缩放图像，使得图像最长边为960
        * scoreThreshold 
            置信度阈值 默认0.1f DB模型后处理box的阈值，低于此阈值的box进行过滤，不显示
        * sdk_use_run_det
            默认为 0 0为使用此模型进行推理1为不适用此模型进行推理
        * sdk_use_run_cls
            0为使用此模型进行推理1为不适用此模型进行推理
        * sdk_use_run_rec=1;
            0为使用此模型进行推理1为不适用此模型进行推理
        
        predictor.init(MainActivity.this, sdk_ModelPath, sdk_labelPath, sdk_useOpencl, sdk_cpuThreadNum,sdk_cpuPowerMode,sdk_detLongSize, scoreThreshold);
            模型初始化
        
        predictor2.setInputImage(inputImage); 
            加载图像数据 图像数据类型      Bitmap inputImage ;

        
        ArrayList<OcrResultModel> test=  predictor2.runModel_SDK(sdk_use_run_det, sdk_use_run_cls, sdk_use_run_rec);
            进行推理 获取结果

        释放模型
        predictor2.releaseModel();
# 3. Demo

public ArrayList<OcrResultModel> apiSDK(Bitmap inputImage){
Predictor predictor2 = new Predictor();
//模型参数配置
String sdk_ModelPath="models/ch_PP-OCRv3";
String sdk_labelPath="labels/ppocr_keys_v1.txt";
int sdk_useOpencl=0;//0 cpu 1gpu
int sdk_cpuThreadNum=4;
String sdk_cpuPowerMode="LITE_POWER_HIGH"; //LITE_POWER_HIGH  LITE_POWER_LOW  LITE_POWER_FULL  LITE_POWER_NO_BIND LITE_POWER_RAND_HIGH LITE_POWER_RAND_LOW
int sdk_detLongSize=960;
float scoreThreshold = 0.1f;
int sdk_use_run_det=1;
int sdk_use_run_cls=1;
int sdk_use_run_rec=1;
//模型初始化
predictor2.init(MainActivity.this, sdk_ModelPath, sdk_labelPath, sdk_useOpencl,
sdk_cpuThreadNum,sdk_cpuPowerMode,sdk_detLongSize, scoreThreshold);

if (predictor2.isLoaded()){
    predictor2.setInputImage(inputImage);
}
ArrayList<OcrResultModel> test=  predictor2.runModel_SDK(sdk_use_run_det, sdk_use_run_cls, sdk_use_run_rec);
predictor2.releaseModel();
return test;
}
# 4. 结果解析
    返回结果为 ArrayList<OcrResultModel>
    每个元素为 OcrResultModel
    每个OcrResultModel 属性为
        cls_condifence 分类置信度 0-100%
 
 # 5.效果
 
 [PaddleLite v2.10](https://github.com/PaddlePaddle/Paddle-Lite/tree/release/v2.10) 进行开发。
 Demo SDK 下载 链接：https://pan.baidu.com/s/1vFHrbYlQC5nrryj_vMhGKQ 提取码：curr 
 
        cls_idx 分类下标 0 :对应90°  1:对应180°
        cls_label 类别信息 :90° 180°
        confidence  文本序列置信度，如果序列越长 并且置信度越高 说明越准，一般会随着长度的增加降低置信度
        label   识别后的文字
        points 4个 ArrayList<Point> 也就是4个坐标点  Point point = new Point(x, y)

        


  
