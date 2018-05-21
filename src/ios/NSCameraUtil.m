//
//  NSCameraUtil.m
//  GoodJob
//
//  Created by 梁仲太 on 2018/5/18.
//

#import "NSCameraUtil.h"

@implementation NSCameraUtil

//获取指定偏离天数的指定格式的日期
+(NSString *)format:(NSString *)format andOffset:(NSInteger)offset{
    NSDate*nowDate = [NSDate date];
    NSDate* theDate;
    NSTimeInterval  oneDay = 24*60*60*1;  //1天的长度
    //之后的天数
    theDate = [nowDate initWithTimeIntervalSinceNow: oneDay*offset ];
    //之前的天数
    //theDate = [nowDate initWithTimeIntervalSinceNow: -oneDay*dis ];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"zh_CN"]];
    //@"MM-dd"
    [dateFormatter setDateFormat:format];
    NSString * currentDateStr = [dateFormatter stringFromDate:theDate];
    return currentDateStr;
}

//测量text文本宽度
+(CGFloat)formatTextWidth:(NSString *)text addFont:(UIFont *)font addHeight:(CGFloat)height{
    if(text.length <= 0){
        return 0;
    }
    UITextView *textView = [[UITextView alloc] init];
    textView.text = text;
    textView.font = font;
    CGSize size = [textView sizeThatFits:CGSizeMake(CGFLOAT_MAX, height)];
    return size.width;
}

//测量text文本高度
+(CGFloat)formatTextHeight:(UIView *)view andText:(NSString *)text andFont:(CGFloat)font andWidth:(CGFloat)width andTextView:(BOOL)tv{
    double version = [[UIDevice currentDevice].systemVersion doubleValue];
    CGSize sizeToFit;
    if(version<7.0){
        sizeToFit = [text sizeWithFont:[UIFont systemFontOfSize:font]
                     constrainedToSize:CGSizeMake(width - (tv?16.0:0), CGFLOAT_MAX)
                         lineBreakMode:NSLineBreakByWordWrapping];
    }else{
        NSAttributedString *attrStr = [[NSAttributedString alloc] initWithString:text];
        if([view.class isKindOfClass:UILabel.class]){
            ((UILabel *)view).attributedText = attrStr;
        }else if([view.class isKindOfClass:UIButton.class]){
            [((UIButton *)view) setAttributedTitle:attrStr forState:UIControlStateNormal];
        }else if([view.class isKindOfClass:UITextView.class]){
            ((UITextView *)view).attributedText = attrStr;
        }else if([view.class isKindOfClass:UITextField.class]){
            ((UITextField *)view).attributedText = attrStr;
        }
        
        NSRange range = NSMakeRange(0, attrStr.length);
        NSDictionary *dic = [attrStr attributesAtIndex:0 effectiveRange:&range];
        sizeToFit = [text boundingRectWithSize:CGSizeMake(width - (tv?16.0:0), MAXFLOAT) options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading attributes:dic context:nil].size;
    }
    return sizeToFit.height + (tv?16.0:0);
}

+(UIImage *)imageWithLogoText:(UIImage *)img andText:(NSString *)text andLeftOffset:(CGFloat)left andBottomOffset:(CGFloat)bottom{
    //注：此为后来更改，用于显示中文。zyq,2013-5-8
    //设置上下文（画布）大小
    CGSize size = CGSizeMake(200, img.size.height);
    //创建一个基于位图的上下文(context)，并将其设置为当前上下文
    UIGraphicsBeginImageContext(size);
    //获取当前上下文
    CGContextRef contextRef = UIGraphicsGetCurrentContext();
    //画布的高度
    CGContextTranslateCTM(contextRef, 0, img.size.height);
    //画布翻转
    CGContextScaleCTM(contextRef, 1.0, -1.0);
    //在上下文种画当前图片
    CGContextDrawImage(contextRef, CGRectMake(0, 0, img.size.width, img.size.height), [img CGImage]);
    //上下文种的文字属性
    [[UIColor redColor] set];
    CGContextTranslateCTM(contextRef, 0, img.size.height);
    CGContextScaleCTM(contextRef, 1.0, -1.0);
    UIFont *font = [UIFont systemFontOfSize:12];
    CGFloat width = [NSCameraUtil formatTextWidth:text addFont:font addHeight:40];
    //此处设置文字显示的位置
    [text drawInRect:CGRectMake(left, img.size.height-bottom, width, 40) withFont:font];
    //从当前上下文种获取图片
    UIImage *targetimg =UIGraphicsGetImageFromCurrentImageContext();
    //移除栈顶的基于当前位图的图形上下文。
    UIGraphicsEndImageContext();
    return targetimg;
}

+(NSString *)getImageSavePath{
    //获取存放的照片
    //获取Documents文件夹目录
    NSArray *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentPath = [path objectAtIndex:0];
    //指定新建文件夹路径
    NSString *imageDocPath = [documentPath stringByAppendingPathComponent:@"PhotoFile"];
    return imageDocPath;
}


+(void)initData{
    //指定新建文件夹路径
    NSString *imageDocPath = [self getImageSavePath];
    //创建ImageFile文件夹
    [[NSFileManager defaultManager] createDirectoryAtPath:imageDocPath withIntermediateDirectories:YES attributes:nil error:nil];
}

+(void)saveImage:(UIImage *)image andPath:(NSString *)path{
    //把图片转成NSData类型的数据来保存文件
    NSData *data = nil;
    //判断图片是不是png格式的文件
    if (UIImagePNGRepresentation(image)) {
        //返回为png图像。
        data = UIImagePNGRepresentation(image);
    }else {
        //返回为JPEG图像。
        data = UIImageJPEGRepresentation(image, 1.0);
    }
    //保存
    [[NSFileManager defaultManager] createFileAtPath:path contents:data attributes:nil];
}

@end
