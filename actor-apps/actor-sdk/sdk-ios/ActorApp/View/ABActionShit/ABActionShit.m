//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//
#import "ABActionShit.h"

@interface ABActionShit () <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) UIView *shadowView;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) UITableView *cancelTableView;

@end

@implementation ABActionShit

- (UIView *)shadowView
{
    if (_shadowView == nil) {
        _shadowView = [[UIView alloc] initWithFrame:self.bounds];
        _shadowView.backgroundColor = [UIColor blackColor];
        _shadowView.alpha = 0.0;
        [_shadowView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(anyTapped:)]];
        [self addSubview:_shadowView];
    }
    return _shadowView;
}

- (UITableView *)tableView
{
    if (_tableView == nil) {
        _tableView = [[UITableView alloc] init];
        _tableView.dataSource = self;
        _tableView.delegate = self;
        _tableView.scrollEnabled = NO;
        _tableView.separatorInset = UIEdgeInsetsZero;
        _tableView.backgroundColor = [UIColor colorWithRed:0xF6/255. green:0xF6/255. blue:0xF0/255. alpha:1.0];
        [self addSubview:_tableView];
    }
    return _tableView;
}

- (UITableView *)cancelTableView
{
    if (_cancelTableView == nil) {
        _cancelTableView = [[UITableView alloc] init];
        _cancelTableView.dataSource = self;
        _cancelTableView.delegate = self;
        _cancelTableView.scrollEnabled = NO;
        [self addSubview:_cancelTableView];
    }
    return _cancelTableView;
}

#pragma mark - Init

- (void)setup
{
    self.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
    self.translatesAutoresizingMaskIntoConstraints = YES;
    self.destructiveButtonIndex = NSNotFound;
    [self shadowView];
    [self tableView];
    [self cancelTableView];
}

- (instancetype)init
{
    if (self = [super initWithFrame:[UIScreen mainScreen].bounds]) {
        [self setup];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {
        [self setup];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    if (self = [super initWithCoder:aDecoder]) {
        [self setup];
    }
    return self;
}

#pragma mark - Table View

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (tableView == self.cancelTableView)
        return 1;
    return self.buttonTitles.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 44;
}

- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([tableView respondsToSelector:@selector(setSeparatorInset:)])
        [tableView setSeparatorInset:UIEdgeInsetsZero];
    if ([tableView respondsToSelector:@selector(setLayoutMargins:)])
        [tableView setLayoutMargins:UIEdgeInsetsZero];
    if ([cell respondsToSelector:@selector(setLayoutMargins:)])
        [cell setLayoutMargins:UIEdgeInsetsZero];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (tableView == self.cancelTableView) {
        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell_cancel"];
        if (cell == nil) {
            cell = [[UITableViewCell alloc] initWithStyle:(UITableViewCellStyleDefault) reuseIdentifier:@"cell_cancel"];
            cell.textLabel.textAlignment = NSTextAlignmentCenter;
            cell.textLabel.font = [UIFont boldSystemFontOfSize:21];
            cell.layer.rasterizationScale = [UIScreen mainScreen].scale;
            cell.layer.shouldRasterize = YES;
        }
        
        cell.textLabel.text = self.cancelButtonTitle ?: @"Cancel";
        cell.textLabel.textColor = self.tintColor;
        
        return cell;
    }
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell_1"];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:(UITableViewCellStyleDefault) reuseIdentifier:@"cell_1"];
        cell.backgroundColor = tableView.backgroundColor;
        cell.contentView.backgroundColor = tableView.backgroundColor;
        cell.textLabel.textAlignment = NSTextAlignmentCenter;
        cell.layer.rasterizationScale = [UIScreen mainScreen].scale;
        cell.layer.shouldRasterize = YES;
    }
    
    cell.textLabel.font = ^{
        if (indexPath.row == self.destructiveButtonIndex)
            return [UIFont boldSystemFontOfSize:21];
        return [UIFont systemFontOfSize:21];
    }();
    
    cell.textLabel.textColor = ^{
        if (indexPath.row == self.destructiveButtonIndex)
            return [UIColor redColor];
        return self.tintColor;
    }();
    
    cell.textLabel.text = self.buttonTitles[indexPath.row];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (tableView == self.cancelTableView) {
        if ([self.delegate respondsToSelector:@selector(actionShitClickedCancelButton:)])
            [self.delegate actionShitClickedCancelButton:self];
    } else {
        if ([self.delegate respondsToSelector:@selector(actionShit:clickedButtonAtIndex:)])
            [self.delegate actionShit:self clickedButtonAtIndex:indexPath.row];
    }
    
    [self hideWithCompletion:nil];
}

#pragma mark - View

- (void)showWithCompletion:(void(^)())completion
{
    CGFloat margin = 8;
    
    self.tableView.frame = CGRectMake(margin, self.bounds.size.height - margin - (44 + margin)*(self.cancelButtonHidden?0:1) - self.buttonTitles.count*44, self.bounds.size.width - 2*margin, self.buttonTitles.count*44);
    self.tableView.layer.masksToBounds = YES;
    self.tableView.layer.cornerRadius = 4;
    
    self.cancelTableView.frame = CGRectMake(margin, self.bounds.size.height -margin - 44, self.bounds.size.width - 2*margin, 44);
    self.cancelTableView.layer.masksToBounds = YES;
    self.cancelTableView.layer.cornerRadius = 4;
    self.cancelTableView.hidden = self.cancelButtonHidden;
    
    [self.tableView reloadData];
    [self.cancelTableView reloadData];
    
    UIWindow *window = [UIApplication sharedApplication].windows.lastObject;
    [window addSubview:self];
    
    self.tableView.transform = CGAffineTransformMakeTranslation(0, (self.buttonTitles.count+3)*44);
    self.cancelTableView.transform = CGAffineTransformMakeTranslation(0, (self.buttonTitles.count+3)*44);
    [UIView animateWithDuration:0.2 delay:0 options:7<<16 animations:^{
        self.shadowView.alpha = 0.4;
        self.tableView.transform = CGAffineTransformIdentity;
        self.cancelTableView.transform = CGAffineTransformIdentity;
    } completion:^(BOOL finished) {
        if (completion)
            completion();
    }];
}

- (void)anyTapped:(id)sender
{
    [self tableView:self.cancelTableView didSelectRowAtIndexPath:nil];
}

- (void)hideWithCompletion:(void(^)())completion
{
    [UIView animateWithDuration:0.2 delay:0 options:7<<16 animations:^{
        self.shadowView.alpha = 0.0;
        self.tableView.transform = CGAffineTransformMakeTranslation(0, (self.buttonTitles.count+3)*44);
        self.cancelTableView.transform = CGAffineTransformMakeTranslation(0, (self.buttonTitles.count+3)*44);    
    } completion:^(BOOL finished) {
        [self removeFromSuperview];
        if (completion)
            completion();
    }];
}

@end
