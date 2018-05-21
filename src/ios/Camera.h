//
//  NSCamera.h
//  GoodJob
//
//  Created by 梁仲太 on 2018/5/17.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <CoreLocation/CLLocationManager.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>

@interface Camera : CDVPlugin
    
-(void)coolMethod:(CDVInvokedUrlCommand *)command;
-(void)successWithMessage:(NSString *)message;
-(void)faileWithMessage:(NSString *)message;
    
    @end
