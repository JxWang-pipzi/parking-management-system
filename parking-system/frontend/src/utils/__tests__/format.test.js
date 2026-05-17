import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { formatDate, formatDateTime, formatTime, formatMoney, formatFileSize, formatRelativeTime } from '../../utils/format'

describe('formatDate', () => {
  it('正常日期字符串', () => {
    expect(formatDate('2024-01-15 10:30:00')).toBe('2024-01-15')
  })

  it('空值返回"-"', () => {
    expect(formatDate(null)).toBe('-')
    expect(formatDate(undefined)).toBe('-')
    expect(formatDate('')).toBe('-')
  })

  it('Date对象', () => {
    const date = new Date(2024, 0, 15, 10, 30, 0)
    expect(formatDate(date)).toBe('2024-01-15')
  })

  it('时间戳', () => {
    const timestamp = new Date(2024, 0, 15, 10, 30, 0).getTime()
    expect(formatDate(timestamp)).toBe('2024-01-15')
  })

  it('无效日期返回"-"', () => {
    expect(formatDate('invalid-date')).toBe('-')
  })
})

describe('formatDateTime', () => {
  it('默认格式 YYYY-MM-DD HH:mm:ss', () => {
    const date = new Date(2024, 0, 15, 10, 30, 45)
    expect(formatDateTime(date)).toBe('2024-01-15 10:30:45')
  })

  it('自定义格式 YYYY-MM-DD', () => {
    const date = new Date(2024, 0, 15, 10, 30, 45)
    expect(formatDateTime(date, 'YYYY-MM-DD')).toBe('2024-01-15')
  })

  it('自定义格式 HH:mm:ss', () => {
    const date = new Date(2024, 0, 15, 10, 30, 45)
    expect(formatDateTime(date, 'HH:mm:ss')).toBe('10:30:45')
  })

  it('空值返回"-"', () => {
    expect(formatDateTime(null)).toBe('-')
    expect(formatDateTime('')).toBe('-')
    expect(formatDateTime(undefined)).toBe('-')
  })

  it('无效日期返回"-"', () => {
    expect(formatDateTime('not-a-date')).toBe('-')
  })
})

describe('formatMoney', () => {
  it('正常金额', () => {
    expect(formatMoney(100)).toBe('100.00')
    expect(formatMoney(99.9)).toBe('99.90')
    expect(formatMoney(1234.567)).toBe('1234.57')
  })

  it('0', () => {
    expect(formatMoney(0)).toBe('0.00')
  })

  it('负数', () => {
    expect(formatMoney(-50)).toBe('-50.00')
    expect(formatMoney(-0.01)).toBe('-0.01')
  })

  it('null和undefined', () => {
    expect(formatMoney(null)).toBe('0.00')
    expect(formatMoney(undefined)).toBe('0.00')
  })

  it('自定义小数位', () => {
    expect(formatMoney(100, 0)).toBe('100')
    expect(formatMoney(100, 3)).toBe('100.000')
  })
})

describe('formatTime', () => {
  it('正常时间', () => {
    const date = new Date(2024, 0, 15, 10, 30, 45)
    expect(formatTime(date)).toBe('10:30:45')
  })

  it('空值返回"-"', () => {
    expect(formatTime(null)).toBe('-')
    expect(formatTime('')).toBe('-')
  })
})

describe('formatRelativeTime', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-01-15T12:00:00'))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('刚刚', () => {
    expect(formatRelativeTime(new Date('2024-01-15T12:00:00'))).toBe('刚刚')
  })

  it('X秒前', () => {
    expect(formatRelativeTime(new Date('2024-01-15T11:59:55'))).toBe('5秒前')
  })

  it('X分钟前', () => {
    expect(formatRelativeTime(new Date('2024-01-15T11:55:00'))).toBe('5分钟前')
  })

  it('X小时前', () => {
    expect(formatRelativeTime(new Date('2024-01-15T07:00:00'))).toBe('5小时前')
  })

  it('X天前', () => {
    expect(formatRelativeTime(new Date('2024-01-10T12:00:00'))).toBe('5天前')
  })

  it('空值返回"-"', () => {
    expect(formatRelativeTime(null)).toBe('-')
    expect(formatRelativeTime('')).toBe('-')
  })

  it('无效日期返回"-"', () => {
    expect(formatRelativeTime('invalid')).toBe('-')
  })
})

describe('formatFileSize', () => {
  it('0字节', () => {
    expect(formatFileSize(0)).toBe('0 B')
  })

  it('KB', () => {
    expect(formatFileSize(1024)).toBe('1.00 KB')
  })

  it('MB', () => {
    expect(formatFileSize(1048576)).toBe('1.00 MB')
  })

  it('GB', () => {
    expect(formatFileSize(1073741824)).toBe('1.00 GB')
  })
})
