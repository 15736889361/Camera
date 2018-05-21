//
//  NSCameraUtil.h
//  GoodJob
//
//  Created by 梁仲太 on 2018/5/18.
//

#import <Foundation/Foundation.h>

@interface NSCameraUtil : NSObject

+(NSString *)format:(NSString *)format andOffset:(NSInteger)offset;
+(CGFloat)formatTextWidth:(NSString *)text addFont:(UIFont *)font addHeight:(CGFloat)height;
+(CGFloat)formatTextHeight:(UIView *)view andText:(NSString *)text andFont:(CGFloat)font andWidth:(CGFloat)width andTextView:(BOOL)tv;
+(UIImage *)imageWithLogoText:(UIImage *)img andText:(NSString *)text andLeftOffset:(CGFloat)left andBottomOffset:(CGFloat)bottom;
+(NSString *)getImageSavePath;
+(void)initData;
+(void)saveImage:(UIImage *)image andPath:(NSString *)path;
@end
