//
//  AACountriesViewController.m
//  ActorClient
//
//  Created by Антон Буков on 22.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "ActorModel.h"
#import "ABPhoneField.h"
#import "AAPhoneViewController.h"
#import "AACountriesViewController.h"

@interface AACountriesViewController () <UITableViewDelegate,UITableViewDataSource,UISearchBarDelegate>

@property (nonatomic, weak) IBOutlet UITableView *tableView;
@property (nonatomic, weak) IBOutlet UISearchBar *searchBar;

@property (nonatomic, strong) NSDictionary *countries;
@property (nonatomic, strong) NSArray *letters;

@end

@implementation AACountriesViewController

- (NSDictionary *)countries
{
    if (_countries.count == 0) {
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];
        for (NSString *iso in [ABPhoneField sortedIsoCodes])
        {
            NSString *countryName = [ABPhoneField countryNameByCountryCode][iso];
            if (self.searchBar.text.length == 0 || [countryName rangeOfString:self.searchBar.text options:NSCaseInsensitiveSearch].location != NSNotFound)
            {
                NSString *countryLetter = [countryName substringToIndex:1];
                if (dict[countryLetter] == nil)
                    dict[countryLetter] = [NSMutableArray array];
                [dict[countryLetter] addObject:@[countryName,iso]];
            }
        }
        _countries = dict;
    }
    return _countries;
}

- (NSArray *)letters
{
    if (_letters == nil) {
        _letters = [self.countries.allKeys sortedArrayUsingSelector:@selector(compare:)];
    }
    return _letters;
}

#pragma mark - Scroll View

/*- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    CGRect rect = self.tableView.tableHeaderView.frame;
    rect.size.width = self.view.bounds.size.width;
    self.tableView.tableHeaderView.frame = rect;
}*/

#pragma mark - Table View

- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView
{
    return [@[@"🔎"] arrayByAddingObjectsFromArray:self.letters];
}

- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index
{
    if (index == 0)
        [tableView setContentOffset:CGPointZero animated:YES];
    return index - 2;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return self.letters.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSArray *arr = self.countries[self.letters[section]];
    return arr.count;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return self.letters[section];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"cell_country"];
    
    NSString *letter = self.letters[indexPath.section];
    NSArray *tuple = self.countries[letter][indexPath.row];
    cell.textLabel.text = [@"+" stringByAppendingString:[ABPhoneField callingCodeByCountryCode][tuple.lastObject]];
    cell.detailTextLabel.text = tuple.firstObject;
    cell.accessoryType = [tuple.lastObject isEqualToString:self.currentIso] ? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *letter = self.letters[indexPath.section];
    NSArray *tuple = self.countries[letter][indexPath.row];
    self.currentIso = tuple.lastObject;
    for (UITableViewCell *cell in tableView.visibleCells)
        cell.accessoryType = UITableViewCellAccessoryNone;
    [tableView cellForRowAtIndexPath:indexPath].accessoryType = UITableViewCellAccessoryCheckmark;
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark - Search Bar

- (BOOL)searchBarShouldBeginEditing:(UISearchBar *)searchBar
{
    [self.searchBar setShowsCancelButton:YES animated:YES];
    return YES;
}

- (BOOL)searchBarShouldEndEditing:(UISearchBar *)searchBar
{
    [self.searchBar setShowsCancelButton:NO animated:YES];
    return YES;
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
{
    [searchBar resignFirstResponder];
    searchBar.text = @"";
    [self.tableView reloadData];
}

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar
{
    [searchBar resignFirstResponder];
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText
{
    self.countries = nil;
    self.letters = nil;
    [self.tableView reloadData];
}

#pragma mark - View

- (void)viewDidLoad
{
    [super viewDidLoad];
}

#pragma mark - Navigation

- (void)willMoveToParentViewController:(UIViewController *)parent
{
    if (parent == nil) {
        AAPhoneViewController *controller = (id)self.navigationController.viewControllers[self.navigationController.viewControllers.count-2];
        controller.currentIso = self.currentIso;
    }
}

@end
