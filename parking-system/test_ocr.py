from playwright.sync_api import sync_playwright
import os

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width': 1400, 'height': 900})

    page.goto('http://localhost:5173/login')
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(1000)

    username_input = page.locator('input[type="text"]')
    password_input = page.locator('input[type="password"]')
    if username_input.count() > 0:
        username_input.first.fill('admin')
    if password_input.count() > 0:
        password_input.first.fill('123456')

    login_btn = page.locator('button:has-text("登录")')
    if login_btn.count() > 0:
        login_btn.first.click()
    page.wait_for_timeout(3000)

    page.goto('http://localhost:5173/admin/vehicle-records')
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(2000)

    entry_btn = page.locator('button:has-text("车辆入场")')
    if entry_btn.count() > 0:
        entry_btn.first.click()
        page.wait_for_timeout(1000)
        print('点击车辆入场按钮成功')

        page.screenshot(path='d:/test_entry_dialog.png', full_page=True)

        file_input = page.locator('input[type="file"]')
        if file_input.count() > 0:
            plate_image = 'd:/test_plate_cn.jpg'
            if os.path.exists(plate_image):
                file_input.first.set_input_files(plate_image)
                print('已上传车牌图片')

                page.wait_for_timeout(3000)

                page.screenshot(path='d:/test_ocr_result.png', full_page=True)

                content = page.content()
                if '鲁A12345' in content:
                    print('OCR识别成功！识别到车牌号：鲁A12345')
                elif 'plateNumber' in content or '车牌' in content:
                    print('OCR已返回结果，检查截图')
                else:
                    print('等待OCR结果...')
                    page.wait_for_timeout(3000)
                    page.screenshot(path='d:/test_ocr_result2.png', full_page=True)

                    content2 = page.content()
                    if '鲁A12345' in content2:
                        print('OCR识别成功！识别到车牌号：鲁A12345')
                    else:
                        print('OCR结果页面内容:', content2[:800])
            else:
                print('测试图片不存在')
        else:
            print('未找到文件上传输入框')
            dialog_content = page.content()
            print('对话框内容:', dialog_content[:500])
    else:
        print('未找到车辆入场按钮')

    browser.close()
