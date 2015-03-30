//
//  AARegViewController.m
//  ActorClient
//
//  Created by Антон Буков on 05.03.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "ActorModel.h"
#import "AARegViewController.h"
#import "ActorClient-Swift.h"

@interface AARegViewController () <UIActionSheetDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate>

@property (nonatomic, weak) IBOutlet UIImageView *avatarImageView;
@property (nonatomic, weak) IBOutlet UITextField *firstNameTextField;
@property (nonatomic, weak) IBOutlet UITextField *lastNameTextField;

@end

@implementation AARegViewController

//MARK: - Action Sheet

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == actionSheet.cancelButtonIndex)
        return;
    
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    picker.sourceType = (buttonIndex == 0) ? UIImagePickerControllerSourceTypeCamera : UIImagePickerControllerSourceTypePhotoLibrary;
    picker.delegate = self;
    [self presentViewController:picker animated:YES completion:nil];
}

//MARK: - Image Picker

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    DZNPhotoEditorViewController *editor = [[DZNPhotoEditorViewController alloc] initWithImage:info[UIImagePickerControllerOriginalImage]];
    editor.cropMode = DZNPhotoEditorViewControllerCropModeCircular;
    editor.acceptBlock = ^(DZNPhotoEditorViewController *_editor, NSDictionary *userInfo) {
        self.avatarImageView.image = userInfo[UIImagePickerControllerEditedImage];
        if (self.avatarImageView.image)
            self.avatarImageView.layer.borderWidth = 0.0;
        _editor.cancelBlock(_editor);
    };
    editor.cancelBlock = ^(DZNPhotoEditorViewController *_editor) {
        [self dismissViewControllerAnimated:YES completion:nil];
    };
    [picker pushViewController:editor animated:YES];
}

//MARK: - Table View

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    UIView *view = self.tableView.tableHeaderView.subviews.firstObject;
    view.layer.transform = CATransform3DMakeTranslation(0, scrollView.contentOffset.y, 0);
}

//MARK: - View

- (IBAction)avatarTapped:(UITapGestureRecognizer *)sender
{
    UIActionSheet *actionSheet = [[UIActionSheet alloc] init];
    actionSheet.delegate = self;
    [actionSheet addButtonWithTitle:@"Take a photo"];
    [actionSheet addButtonWithTitle:@"Select from library"];
    actionSheet.cancelButtonIndex = [actionSheet addButtonWithTitle:@"Cancel"];
    [actionSheet showInView:self.view];
}

- (IBAction)doneTapped:(id)sender
{
    if (self.firstNameTextField.text.length == 0) {
        [SVProgressHUD showErrorWithStatus:@"Enter your name"];
        return;
    }
    
    NSMutableArray *arr = [NSMutableArray array];
    if (self.firstNameTextField.text.length)
        [arr addObject:self.firstNameTextField.text];
    if (self.lastNameTextField.text.length)
        [arr addObject:self.lastNameTextField.text];
    NSString *name = [arr componentsJoinedByString:@" "];
    
    NSString *avatarPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"avatar.jpg"];
    [UIImageJPEGRepresentation(self.avatarImageView.image, 0.8) writeToFile:avatarPath atomically:YES];
    
    [SVProgressHUD showWithStatus:@"Saving profile"];
    id<AMCommand> cmd = [[CocoaMessenger messenger] signUpWithNSString:name withNSString:self.avatarImageView.image ? avatarPath : nil withBoolean:NO];
    [cmd startWithAMCommandCallback:MM_CREATE_ALWAYS(^(Class class){
        [class addMethod:@selector(onResultWithId:)
            fromProtocol:@protocol(AMCommandCallback)
                blockImp:^(id this,id res){
                    NSLog(@"%@", res);
                    /*
                     if (res.ordinal == AMAuthState_LOGGED_IN) {
                     [SVProgressHUD showSuccessWithStatus:@"LOGGED_IN"];
                     [self.navigationController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
                     } else {
                     [SVProgressHUD showSuccessWithStatus:[res description]];
                     [self performSegueWithIdentifier:@"segue_reg" sender:nil];
                     }*/
                    [SVProgressHUD dismiss];
                    [self.navigationController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
                }];
        [class addMethod:@selector(onErrorWithJavaLangException:)
            fromProtocol:@protocol(AMCommandCallback)
                blockImp:^(id this,JavaLangException *e){
                    NSLog(@"onErrorWithJavaLangException: %@",e);
                    [SVProgressHUD showErrorWithStatus:[NSString stringWithFormat:@"Error: %@",e.getLocalizedMessage]];
                }];
    })];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if ([UIApplication sharedApplication].keyWindow.bounds.size.height == 480) {
        CGRect rect = self.tableView.tableHeaderView.frame;
        rect.size.height = 90;
        self.tableView.tableHeaderView.frame = rect;
    }
    
    self.avatarImageView.layer.cornerRadius = self.avatarImageView.bounds.size.width/2;
    self.avatarImageView.layer.masksToBounds = YES;
    self.avatarImageView.layer.borderColor = [UIColor colorWithRed:217/255. green:217/255. blue:217/255. alpha:1.0].CGColor;
    self.avatarImageView.layer.borderWidth = 1.0;
}

- (void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    
    self.tableView.contentInset = UIEdgeInsetsZero;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.firstNameTextField becomeFirstResponder];
}

@end
