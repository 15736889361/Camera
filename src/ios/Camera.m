//
//  NSCamera.m
//  GoodJob
//
//  Created by 梁仲太 on 2018/5/17.
//

#import "Camera.h"
#import "NSCameraUtil.h"
#import "MoLocationManager.h"

@interface Camera()<UIImagePickerControllerDelegate,UINavigationBarDelegate>
    
    @property(nonatomic,copy)NSString *callbackId;
    @property(nonatomic,copy)NSString *name;
    @property(nonatomic,assign)CGFloat compression;
    
    @end

@implementation Camera
    
-(void)coolMethod:(CDVInvokedUrlCommand *)command{
    
    self.callbackId = command.callbackId;
    self.name = [command.arguments objectAtIndex:0];
    if(command.arguments.count>1){
        NSInteger compression = [[command.arguments objectAtIndex:1] integerValue];
        self.compression = compression/100.0;
    }
    self.compression = self.compression<=0?0.8:self.compression;
    //判断权限
    //检查摄像头是否可用
    BOOL useCamera = [UIImagePickerController isCameraDeviceAvailable:UIImagePickerControllerCameraDeviceRear];
    
    if (!useCamera) {
        [self faileWithMessage:@"摄像头不可用"];
        return;
    }
    
    
    //打开相机
    UIImagePickerControllerSourceType sourceType;
    sourceType = UIImagePickerControllerSourceTypeCamera;
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.delegate = self;
    picker.allowsEditing = NO;
    picker.sourceType = sourceType;
    [self.viewController presentViewController:picker animated:YES completion:nil];
}
    
-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info{
    //获取照片
    NSString *mediaType = [info objectForKey:UIImagePickerControllerMediaType];
    // 判断获取类型：图片
    if ([@"public.image" isEqualToString:mediaType]){
        //获取原始照片
        __block  UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];
        //获取时间
        __block  NSString *date = [NSCameraUtil format:@"yyyy-MM-dd HH:mm:ss" andOffset:0];
        //获取经纬度
        __block  BOOL isOnece = YES;
        [MoLocationManager getMoLocationWithSuccess:^(double lat, double lng){
            isOnece = NO;
            //只打印一次经纬度
            NSLog(@"lat lng (%f, %f)", lat, lng);
            NSString *location = [NSString stringWithFormat:@"%f%f",lat,lng];
            //加水印
            image = [NSCameraUtil imageWithLogoText:image andText:[NSString stringWithFormat:@"%@%@",date,location] andLeftOffset:14 andBottomOffset:68];
            image = [NSCameraUtil imageWithLogoText:image andText:self.name andLeftOffset:14 andBottomOffset:14];
            
            //压缩图片
            NSData *data = UIImageJPEGRepresentation(image, self.compression);
            UIImage *compressionImage = [UIImage imageWithData:data];
            
            //保存到本地
            NSString *path = [NSCameraUtil getImageSavePath];
            [NSCameraUtil saveImage:compressionImage andPath:path];
            //将地址回传给js
            [self successWithMessage:path];
            if (!isOnece) {
                [MoLocationManager stop];
            }
        } Failure:^(NSError *error){
            isOnece = NO;
            [self faileWithMessage:@"定位失败!"];
            if (!isOnece) {
                [MoLocationManager stop];
            }
        }];
    }else{
        [self faileWithMessage:@"不支持视频类型"];
    }
    
}
    
-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    [self faileWithMessage:@"cancel"];
}
    
-(void)successWithMessage:(NSString *)message{
    if(self.callbackId==nil)return;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}
    
-(void)faileWithMessage:(NSString *)message{
    if(self.callbackId==nil)return;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}
    
    @end

