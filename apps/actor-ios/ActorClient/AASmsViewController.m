//
//  AASmsViewController.m
//  ActorClient
//
//  Created by Антон Буков on 22.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "ActorModel.h"
#import "AASmsViewController.h"
#import "ActorClient-Swift.h"

@interface AASmsViewController ()

@property (nonatomic, weak) IBOutlet UITextField *codeTextField;
@property (nonatomic, weak) IBOutlet UILabel *phoneLabel;

@end

@implementation AASmsViewController

#pragma mark - Scroll View

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    // Freezed table view header position CGPointZero
    UIView *view = self.tableView.tableHeaderView.subviews.firstObject;
    view.layer.transform = CATransform3DMakeTranslation(0, scrollView.contentOffset.y, 0);
}

#pragma mark - View

- (IBAction)nextTapped:(UIBarButtonItem *)button
{
    [SVProgressHUD showWithMaskType:(SVProgressHUDMaskTypeBlack)];
    
    AMMessenger* messenger = [CocoaMessenger messenger];
    
    id<AMCommand> cmd = [messenger sendCodeWithInt:[self.codeTextField.text intValue]];
    [cmd startWithAMCommandCallback:MM_CREATE_ALWAYS(^(Class class){
        [class addMethod:@selector(onResultWithId:)
            fromProtocol:@protocol(AMCommandCallback)
                blockImp:^(id this,AMAuthStateEnum *res){
                    if (res.ordinal == AMAuthState_LOGGED_IN) {
                        [SVProgressHUD showSuccessWithStatus:@"LOGGED_IN"];
                        [self.navigationController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
                    } else {
                        [SVProgressHUD showSuccessWithStatus:[res description]];
                        [self performSegueWithIdentifier:@"segue_reg" sender:nil];
                    }
                }];
        [class addMethod:@selector(onErrorWithJavaLangException:)
            fromProtocol:@protocol(AMCommandCallback)
                blockImp:^(id this,JavaLangException *e){
                    NSLog(@"onErrorWithJavaLangException: %@",e);
                    [SVProgressHUD showErrorWithStatus:[NSString stringWithFormat:@"Error: %@",e.getLocalizedMessage]];
                }];
    })];
}

- (IBAction)codeChanged:(UITextField *)textField
{
    if (textField.text.length == 6)
        [self shouldPerformSegueWithIdentifier:@"segue_next" sender:nil];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.phoneLabel.text = self.formattedPhoneNumber;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.codeTextField becomeFirstResponder];
}

@end
