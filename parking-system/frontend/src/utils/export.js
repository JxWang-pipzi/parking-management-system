/**
 * 导出Excel文件
 * @param {Array} data 数据
 * @param {Array} columns 列配置
 * @param {string} filename 文件名
 */
export function exportToExcel(data, columns, filename = 'export.xlsx') {
  // 创建表头
  const headers = columns.map(col => col.label)
  
  // 创建数据行
  const rows = data.map(row => {
    return columns.map(col => {
      const value = row[col.prop]
      return value !== null && value !== undefined ? value : ''
    })
  })
  
  // 合并表头和数据
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  // 创建Blob
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  
  // 创建下载链接
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', filename.replace('.xlsx', '.csv'))
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 导出JSON文件
 * @param {Object|Array} data 数据
 * @param {string} filename 文件名
 */
export function exportToJSON(data, filename = 'export.json') {
  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: 'application/json' })
  
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
