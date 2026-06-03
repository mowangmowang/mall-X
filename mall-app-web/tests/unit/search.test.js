import { searchProductList } from '@/api/product.js';
import { shouldFallback } from '@/utils/errorHandler.js';

// 模拟 API 响应
jest.mock('@/api/product.js');

describe('Product Search Fallback Logic', () => {
  it('should fallback to MySQL when ES has network error', () => {
    const networkError = { errMsg: 'request:fail timeout' };
    expect(shouldFallback(networkError)).toBe(true);
  });

  it('should fallback to MySQL when ES returns 503', () => {
    const serverError = { statusCode: 503 };
    expect(shouldFallback(serverError)).toBe(true);
  });

  it('should NOT fallback when ES returns business error (400)', () => {
    const businessError = { statusCode: 400, data: { message: 'Invalid keyword' } };
    expect(shouldFallback(businessError)).toBe(false);
  });
});
