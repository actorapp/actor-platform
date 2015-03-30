//
//  AAPhoneViewController.m
//  ActorClient
//
//  Created by Антон Буков on 20.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "ActorModel.h"
#import "ABPhoneField.h"
#import "AACountriesViewController.h"
#import "AASmsViewController.h"
#import "AAPhoneViewController.h"
#import "AAPhoneViewController.h"
#import "ActorClient-Swift.h"

@interface AAPhoneViewController ()

@property (nonatomic, weak) IBOutlet UILabel *countryNameLabel;
@property (nonatomic, weak) IBOutlet UILabel *countryPrefixLabel;
@property (nonatomic, weak) IBOutlet ABPhoneField *phoneTextField;

@end

@implementation AAPhoneViewController

- (void)setCurrentIso:(NSString *)currentIso
{
    _currentIso = currentIso;
    self.phoneTextField.currentIso = currentIso;
    self.countryPrefixLabel.text = [@"+" stringByAppendingString:[ABPhoneField callingCodeByCountryCode][currentIso]];
    self.countryNameLabel.text = [ABPhoneField countryNameByCountryCode][currentIso];
}

#pragma mark - Scroll View

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    // Freezed table view header position CGPointZero
    UIView *view = self.tableView.tableHeaderView.subviews.firstObject;
    view.layer.transform = CATransform3DMakeTranslation(0, scrollView.contentOffset.y, 0);
}

#pragma mark - Table View

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 2 && indexPath.section == 0) {
        return MIN(120,[UIScreen mainScreen].bounds.size.height
                       - tableView.tableHeaderView.frame.size.height
                       - [self tableView:tableView heightForRowAtIndexPath:
                          [NSIndexPath indexPathForRow:0 inSection:0]]
                       - [self tableView:tableView heightForRowAtIndexPath:
                          [NSIndexPath indexPathForRow:1 inSection:0]]
                       - 216/* Number Pad */);
    }
    return [super tableView:tableView heightForRowAtIndexPath:indexPath];
}

#pragma mark - View

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    // iPhone 3.5" should have shorter header
    if ([UIScreen mainScreen].bounds.size.height == 480) {
        self.tableView.tableHeaderView.frame = CGRectMake(0, 0, self.tableView.tableHeaderView.frame.size.width, 90);
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.currentIso = self.phoneTextField.currentIso;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.phoneTextField becomeFirstResponder];
}

#pragma mark - Navigation

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if ([identifier isEqualToString:@"segue_next"]) {
        if (self.phoneTextField.phoneNumber.length < [[ABPhoneField phoneMinLengthByCountryCode][self.currentIso] integerValue])
        {
            NSLog(@"Phone number too short");
            return NO;
        }
        
        [SVProgressHUD showWithMaskType:(SVProgressHUDMaskTypeBlack)];
        id<AMCommand> cmd = [[CocoaMessenger messenger] requestSmsWithLong:self.phoneTextField.phoneNumber.longLongValue];
        [cmd startWithAMCommandCallback:MM_ANON(^(MMAnonymousClass *anon){
            [anon addMethod:@selector(onResultWithId:)
               fromProtocol:@protocol(AMCommandCallback)
                  blockImp:^(id this,id res){
                       [self performSegueWithIdentifier:identifier sender:sender];
                       [SVProgressHUD dismiss];
                   }];
            [anon addMethod:@selector(onErrorWithJavaLangException:)
               fromProtocol:@protocol(AMCommandCallback)
                   blockImp:^(id this,JavaLangException *e){
                       NSLog(@"onErrorWithJavaLangException: %@",e);
                       [SVProgressHUD showErrorWithStatus:[NSString stringWithFormat:@"Error: %@",e.getLocalizedMessage]];
                   }];
        })];
        
        return NO;
    }
    return YES;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"segue_countries"]) {
        AACountriesViewController *controller = segue.destinationViewController;
        controller.currentIso = self.currentIso;
    }
    if ([segue.identifier isEqualToString:@"segue_next"]) {
        AASmsViewController *controller = segue.destinationViewController;
        controller.formattedPhoneNumber = self.phoneTextField.formattedPhoneNumber;
    }
}

@end
