import * as XLSX from 'xlsx'

export function exportToExcel(data, fileName) {
  try {
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    
    // 简单直接的实现：只处理数组数据
    let exportData = []
    
    // 检查data是否为数组
    if (Array.isArray(data)) {
      exportData = data
    } else if (typeof data === 'object' && data !== null) {
      // 如果是对象，尝试获取第一个工作表的数据
      const firstSheetName = Object.keys(data)[0]
      if (firstSheetName && Array.isArray(data[firstSheetName])) {
        exportData = data[firstSheetName]
      }
    }
    
    // 确保数据是有效的数组
    if (!Array.isArray(exportData)) {
      exportData = []
    }
    
    // 创建工作表并添加到工作簿
    const ws = XLSX.utils.json_to_sheet(exportData)
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    
    // 导出文件
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    
  } catch (error) {
    console.error('导出Excel失败:', error)
    throw error
  }
}