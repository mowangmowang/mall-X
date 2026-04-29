<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProductListAPI,
  productUpdateDeleteStatusAPI,
  productUpdateNewStatusAPI,
  productUpdateRecommendStatusAPI,
  productUpdatePublishStatusAPI,
  getProductVertifyRecordAPI,
  productUpdateVerifyStatusAPI
} from '@/apis/product'
import { getSkuListByPidAPI, skuUpdateByPidAPI } from '@/apis/skuStock'
import { getProductAttributeListAPI } from '@/apis/productAttr'
import { getBrandListAPI } from '@/apis/brand'
import { getProductCategoryListWithChildrenAPI } from '@/apis/productCate'
import { Search, Tickets, Edit } from '@element-plus/icons-vue'
import type { PmsProduct, ProductQueryParam } from '@/types/product'
import type { ElCascaderDataVo, ElSelectDataVo } from '@/types/common'
import type { PmsSkuStock } from '@/types/skuStock'
import type { PmsProductAttribute } from '@/types/productAttr'

// 获取路由
const router = useRouter()

// 列表查询参数
const listQuery = ref<ProductQueryParam>({
  pageNum: 1,
  pageSize: 10
})
// 列表数据
const list = ref<PmsProduct[]>([])
// 总条数
const total = ref(0)
// 加载状态
const listLoading = ref(true)
// 获取列表数据
const getList = async () => {
  listLoading.value = true
  try {
    const response = await getProductListAPI(listQuery.value)
    listLoading.value = false
    list.value = response.data.list
    total.value = response.data.total
    // 加载所有商品的审核状态
    await loadAllProductVerifyStatus()
  } catch (error) {
    listLoading.value = false
    console.error(error)
  }
}
// 筛选搜索中的品牌数据
const brandOptions = ref<ElSelectDataVo[]>([])
// 获取品牌列表数据
const getBrandList = async () => {
  const res = await getBrandListAPI({ pageNum: 1, pageSize: 100 })
  brandOptions.value = res.data.list.map(item => ({ label: item.name, value: item.id!.toString() }))
}
// 筛选搜索中的商品分类数据
const productCateOptions = ref<ElCascaderDataVo[]>([])
// 筛选搜索中当前选中的商品分类，结构为：[父分类ID,分类ID]
const selectProductCateValue = ref([])
// 获取商品分类列表数据
const getProductCateList = async () => {
  const res = await getProductCategoryListWithChildrenAPI()
  const list = res.data
  productCateOptions.value = list.map(item => ({
    label: item.name,
    value: item.id!,
    children: item.children?.map(it => ({ label: it.name, value: it.id! }))
  }))
}
// 筛选搜索中的上下架状态
const publishStatusOptions = ref([
  {
    value: 1,
    label: '上架'
  },
  {
    value: 0,
    label: '下架'
  }
])
// 筛选搜索中的审核状态
const verifyStatusOptions = ref([
  {
    value: 1,
    label: '审核通过'
  },
  {
    value: 0,
    label: '未审核'
  }
])

// 监听改变，通过修改列表查询中的productCategoryId数据
watch(selectProductCateValue, (newValue) => {
  if (newValue != null && newValue.length == 2) {
    listQuery.value.productCategoryId = newValue[1]
  } else {
    listQuery.value.productCategoryId = undefined
  }
}, { immediate: true })

// 组件挂载后执行
onMounted(() => {
  getList()
  getBrandList()
  getProductCateList()
})

// 批量操作类型
const operates = ref([
  {
    label: "商品上架",
    value: "publishOn"
  },
  {
    label: "商品下架",
    value: "publishOff"
  },
  {
    label: "设为推荐",
    value: "recommendOn"
  },
  {
    label: "取消推荐",
    value: "recommendOff"
  },
  {
    label: "设为新品",
    value: "newOn"
  },
  {
    label: "取消新品",
    value: "newOff"
  },
  {
    label: "移入回收站",
    value: "recycle"
  },
  {
    label: "审核通过",
    value: "verifyPass"
  },
  {
    label: "审核不通过",
    value: "verifyReject"
  }
])
// 当前批量操作
const operateType = ref<string>()
// 当前选中的条目
const multipleSelection = ref<PmsProduct[]>([])

// SKU库存弹框数据
const editSkuInfo = reactive({
  dialogVisible: false,
  productId: 0,
  productSn: '',
  productAttributeCategoryId: 0,
  stockList: [] as PmsSkuStock[],
  productAttr: [] as PmsProductAttribute[],
  keyword: undefined
})

// 从PmsSkuStock的spData中获取规格对应的值
const getProductSkuSp = (row: PmsSkuStock, index: number) => {
  const spData = JSON.parse(row.spData!)
  if (spData && index < spData.length) {
    return spData[index].value
  } else {
    return ''
  }
}

const handleShowSkuEditDialog = async (index: number, row: PmsProduct) => {
  editSkuInfo.dialogVisible = true
  editSkuInfo.productId = row.id!
  editSkuInfo.productSn = row.productSn
  editSkuInfo.productAttributeCategoryId = row.productAttributeCategoryId!
  editSkuInfo.keyword = undefined
  const resp = await getSkuListByPidAPI(row.id!, { keyword: editSkuInfo.keyword })
  editSkuInfo.stockList = resp.data
  if (row.productAttributeCategoryId) {
    const res2 = await getProductAttributeListAPI(row.productAttributeCategoryId, { pageNum: 1, pageSize: 10, type: 0 })
    editSkuInfo.productAttr = res2.data.list
  }
}

const handleSearchEditSku = async () => {
  const response = await getSkuListByPidAPI(editSkuInfo.productId, { keyword: editSkuInfo.keyword })
  editSkuInfo.stockList = response.data
}

const handleEditSkuConfirm = async () => {
  if (!editSkuInfo.stockList || editSkuInfo.stockList.length <= 0) {
    ElMessage({
      message: '暂无sku信息',
      type: 'warning',
      duration: 1000
    })
    return
  }
  await ElMessageBox.confirm('是否要进行修改', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await skuUpdateByPidAPI(editSkuInfo.productId, editSkuInfo.stockList)
  ElMessage({
    message: '修改成功',
    type: 'success',
    duration: 1000
  })
  editSkuInfo.dialogVisible = false
}

const handleSearchList = () => {
  listQuery.value.pageNum = 1
  getList()
}

const handleAddProduct = () => {
  router.push({ path: '/pms/addProduct' })
}

const handleBatchOperate = async () => {
  if (!operateType.value) {
    ElMessage({
      message: '请选择操作类型',
      type: 'warning',
      duration: 1000
    })
    return
  }
  if (!multipleSelection.value || multipleSelection.value.length < 1) {
    ElMessage({
      message: '请选择要操作的商品',
      type: 'warning',
      duration: 1000
    })
    return
  }
  await ElMessageBox.confirm('是否要进行该批量操作?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  const ids = multipleSelection.value.map(item => item.id!)
  switch (operateType.value) {
    case operates.value[0]!.value:
      updatePublishStatus(1, ids)
      break
    case operates.value[1]!.value:
      updatePublishStatus(0, ids)
      break
    case operates.value[2]!.value:
      updateRecommendStatus(1, ids)
      break
    case operates.value[3]!.value:
      updateRecommendStatus(0, ids)
      break
    case operates.value[4]!.value:
      updateNewStatus(1, ids)
      break
    case operates.value[5]!.value:
      updateNewStatus(0, ids)
      break
    case operates.value[6]!.value:
      updateDeleteStatus(1, ids)
      break
    case operates.value[7]!.value:
      // 审核通过
      await handleVerifyProducts(ids, 1)
      break
    case operates.value[8]!.value:
      // 审核不通过
      await handleVerifyProducts(ids, 2)
      break
    default:
      break
  }
  getList()
}

const handleSizeChange = (val: number) => {
  listQuery.value.pageNum = 1
  listQuery.value.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  listQuery.value.pageNum = val
  getList()
}

const handleSelectionChange = (val: PmsProduct[]) => {
  multipleSelection.value = val
}

const handlePublishStatusChange = async (index: number, row: PmsProduct) => {
  await updatePublishStatus(row.publishStatus!, [row.id!])
}

const handleNewStatusChange = async (index: number, row: PmsProduct) => {
  await updateNewStatus(row.newStatus!, [row.id!])
}

const handleRecommendStatusChange = async (index: number, row: PmsProduct) => {
  await updateRecommendStatus(row.recommandStatus!, [row.id!])
}

const handleResetSearch = () => {
  selectProductCateValue.value = []
  listQuery.value = { pageNum: 1, pageSize: 10 }
}

const handleDelete = async (index: number, row: PmsProduct) => {
  updateDeleteStatus(1, [row.id!])
}

const handleUpdateProduct = (index: number, row: PmsProduct) => {
  router.push({ path: '/pms/updateProduct', query: { id: row.id } })
}

// 查看商品详情
const handleShowProduct = (index: number, row: PmsProduct) => {
  router.push({ path: '/pms/updateProduct', query: { id: row.id } })
}

// 审核详情弹框
const verifyDialogVisible = ref(false)
const verifyRecordList = ref<any[]>([])

// 存储每个商品的审核状态（用于列表显示）
const productVerifyStatusMap = ref<Map<number, string>>(new Map())

const handleShowVerifyDetail = async (index: number, row: PmsProduct) => {
  try {
    const res = await getProductVertifyRecordAPI(row.id!)
    verifyRecordList.value = res.data || []
    verifyDialogVisible.value = true
    
    // 更新该商品的审核状态映射
    if (verifyRecordList.value.length > 0) {
      const latestRecord = verifyRecordList.value[verifyRecordList.value.length - 1]
      if (latestRecord.status === 1) {
        productVerifyStatusMap.value.set(row.id!, '审核通过')
      } else if (latestRecord.status === 2) {
        productVerifyStatusMap.value.set(row.id!, '审核不通过')
      }
    }
  } catch (error) {
    console.error('获取审核记录失败:', error)
    ElMessage({
      message: '获取审核记录失败',
      type: 'error',
      duration: 1000
    })
  }
}

// 加载所有商品的审核状态
const loadAllProductVerifyStatus = async () => {
  // 为每个商品加载审核记录
  for (const product of list.value) {
    if (product.id && !productVerifyStatusMap.value.has(product.id)) {
      try {
        const res = await getProductVertifyRecordAPI(product.id)
        const records = res.data || []
        if (records.length > 0) {
          const latestRecord = records[records.length - 1]
          if (latestRecord.status === 1) {
            productVerifyStatusMap.value.set(product.id, '审核通过')
          } else if (latestRecord.status === 2) {
            productVerifyStatusMap.value.set(product.id, '审核不通过')
          }
        }
      } catch (error) {
        console.error(`加载商品${product.id}审核状态失败:`, error)
      }
    }
  }
}

// 审核操作对话框
const verifyOperateDialogVisible = ref(false)
const verifyOperateData = reactive({
  ids: [] as number[],
  verifyStatus: 1,
  detail: ''
})

// 处理商品审核
const handleVerifyProducts = async (ids: number[], verifyStatus: number) => {
  verifyOperateData.ids = ids
  verifyOperateData.verifyStatus = verifyStatus
  verifyOperateData.detail = ''
  verifyOperateDialogVisible.value = true
}

// 确认审核操作
const handleVerifyConfirm = async () => {
  if (!verifyOperateData.detail) {
    ElMessage({
      message: '请输入审核详情',
      type: 'warning',
      duration: 1000
    })
    return
  }
  
  try {
    await productUpdateVerifyStatusAPI({
      ids: verifyOperateData.ids.join(','),
      verifyStatus: verifyOperateData.verifyStatus,
      detail: verifyOperateData.detail
    })
    ElMessage({
      message: verifyOperateData.verifyStatus === 1 ? '审核通过' : '审核不通过',
      type: 'success',
      duration: 1000
    })
    verifyOperateDialogVisible.value = false
    // 清空审核状态缓存，重新加载
    productVerifyStatusMap.value.clear()
    getList()
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage({
      message: '审核操作失败',
      type: 'error',
      duration: 1000
    })
  }
}

// 日志弹框
const logDialogVisible = ref(false)
const logList = ref<any[]>([])

const handleShowLog = (index: number, row: PmsProduct) => {
  // 暂时使用审核记录作为日志展示
  logList.value = [
    {
      createTime: new Date().toLocaleString(),
      content: `查看商品：${row.name}`,
      operator: 'admin'
    }
  ]
  logDialogVisible.value = true
}

const updatePublishStatus = async (publishStatus: number, ids: number[]) => {
  await productUpdatePublishStatusAPI({ ids: ids.join(','), publishStatus: publishStatus })
  ElMessage({
    message: '修改成功',
    type: 'success',
    duration: 1000
  })
}

const updateNewStatus = async (newStatus: number, ids: number[]) => {
  await productUpdateNewStatusAPI({ ids: ids.join(','), newStatus: newStatus })
  ElMessage({
    message: '修改成功',
    type: 'success',
    duration: 1000
  })
}

const updateRecommendStatus = async (recommendStatus: number, ids: number[]) => {
  await productUpdateRecommendStatusAPI({ ids: ids.join(','), recommendStatus: recommendStatus })
  ElMessage({
    message: '修改成功',
    type: 'success',
    duration: 1000
  })
}

const updateDeleteStatus = async (deleteStatus: number, ids: number[]) => {
  await productUpdateDeleteStatusAPI({ ids: ids.join(','), deleteStatus: deleteStatus })
  ElMessage({
    message: '删除成功',
    type: 'success',
    duration: 1000
  })
  getList()
}

// 过滤器函数
const verifyStatusFilter = (value: number, row?: any) => {
  // 优先使用审核记录映射中的状态
  if (row && row.id && productVerifyStatusMap.value.has(row.id)) {
    return productVerifyStatusMap.value.get(row.id)
  }
  
  // 默认根据 verifyStatus 字段判断：0->未审核;1->审核通过;2->审核不通过
  if (value === 1) {
    return '审核通过'
  } else if (value === 2) {
    return '审核不通过'
  } else if (value === 0) {
    return '未审核'
  } else {
    return '未知状态'
  }
}
</script>

<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <div>
        <el-icon class="el-icon-middle">
          <Search />
        </el-icon>
        <span>筛选搜索</span>
        <el-button style="float: right" @click="handleSearchList()" type="primary">
          查询结果
        </el-button>
        <el-button style="float: right;margin-right: 15px" @click="handleResetSearch()">
          重置
        </el-button>
      </div>
      <div style="margin-top: 20px">
        <el-form :inline="true" :model="listQuery" label-width="140px">
          <el-form-item label="输入搜索：">
            <el-input style="width: 203px" v-model="listQuery.keyword" placeholder="商品名称"></el-input>
          </el-form-item>
          <el-form-item label="商品货号：">
            <el-input style="width: 203px" v-model="listQuery.productSn" placeholder="商品货号"></el-input>
          </el-form-item>
          <el-form-item label="商品分类：">
            <el-cascader clearable v-model="selectProductCateValue" :options="productCateOptions">
            </el-cascader>
          </el-form-item>
          <el-form-item label="商品品牌：">
            <el-select v-model="listQuery.brandId" placeholder="请选择品牌" clearable style="width: 203px;">
              <el-option v-for="item in brandOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="上架状态：">
            <el-select v-model="listQuery.publishStatus" placeholder="全部" clearable style="width: 203px;">
              <el-option v-for="item in publishStatusOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态：">
            <el-select v-model="listQuery.verifyStatus" placeholder="全部" clearable style="width: 203px;">
              <el-option v-for="item in verifyStatusOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <el-card class="operate-container" shadow="never">
      <el-icon class="el-icon-middle">
        <Tickets />
      </el-icon>
      <span>数据列表</span>
      <el-button class="btn-add" @click="handleAddProduct()">
        添加
      </el-button>
    </el-card>
    <div class="table-container">
      <el-table ref="productTable" :data="list" style="width: 100%" @selection-change="handleSelectionChange"
        v-loading="listLoading" border>
        <el-table-column type="selection" width="60" align="center"></el-table-column>
        <el-table-column label="编号" width="100" align="center">
          <template #default="scope">{{ scope.row.id }}</template>
        </el-table-column>
        <el-table-column label="商品图片" width="120" align="center">
          <template #default="scope"><img style="height: 80px" :src="scope.row.pic"></template>
        </el-table-column>
        <el-table-column label="商品名称" align="center">
          <template #default="scope">
            <p>{{ scope.row.name }}</p>
            <p>品牌：{{ scope.row.brandName }}</p>
          </template>
        </el-table-column>
        <el-table-column label="价格/货号" width="120" align="center">
          <template #default="scope">
            <p>价格：￥{{ scope.row.price }}</p>
            <p>货号：{{ scope.row.productSn }}</p>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="140" align="center">
          <template #default="scope">
            <p style="margin: 6px 0px;">上架：
              <el-switch @change="handlePublishStatusChange(scope.$index, scope.row)" :active-value="1"
                :inactive-value="0" v-model="scope.row.publishStatus">
              </el-switch>
            </p>
            <p style="margin: 6px 0px;">新品：
              <el-switch @change="handleNewStatusChange(scope.$index, scope.row)" :active-value="1" :inactive-value="0"
                v-model="scope.row.newStatus">
              </el-switch>
            </p>
            <p style="margin: 6px 0px;">推荐：
              <el-switch @change="handleRecommendStatusChange(scope.$index, scope.row)" :active-value="1"
                :inactive-value="0" v-model="scope.row.recommandStatus">
              </el-switch>
            </p>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="100" align="center">
          <template #default="scope">{{ scope.row.sort }}</template>
        </el-table-column>
        <el-table-column label="SKU库存" width="100" align="center">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" size="large"
              @click="handleShowSkuEditDialog(scope.$index, scope.row)" circle></el-button>
          </template>
        </el-table-column>
        <el-table-column label="销量" width="100" align="center">
          <template #default="scope">{{ scope.row.sale }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="scope">
            <p>{{ verifyStatusFilter(scope.row.verifyStatus) }}</p>
            <p>
              <el-button type="primary" link @click="handleShowVerifyDetail(scope.$index, scope.row)">审核详情
              </el-button>
            </p>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <p>
              <el-button size="small" @click="handleShowProduct(scope.$index, scope.row)">查看
              </el-button>
              <el-button size="small" @click="handleUpdateProduct(scope.$index, scope.row)">编辑
              </el-button>
            </p>
            <p>
              <el-button size="small" @click="handleVerifyProducts([scope.row.id!], 1)">通过
              </el-button>
              <el-button size="small" type="danger" @click="handleVerifyProducts([scope.row.id!], 2)">拒绝
              </el-button>
            </p>
            <p>
              <el-button size="small" @click="handleShowLog(scope.$index, scope.row)">日志
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.$index, scope.row)">删除
              </el-button>
            </p>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="batch-operate-container">
      <el-select v-model="operateType" placeholder="批量操作">
        <el-option v-for="item in operates" :key="item.value" :label="item.label" :value="item.value">
        </el-option>
      </el-select>
      <el-button style="margin-left: 20px" class="search-button" @click="handleBatchOperate()" type="primary">
        确定
      </el-button>
    </div>
    <div class="pagination-container">
      <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
        layout="total, sizes,prev, pager, next,jumper" :page-size="listQuery.pageSize" :page-sizes="[5, 10, 15]"
        v-model:current-page="listQuery.pageNum" :total="total">
      </el-pagination>
    </div>
    <el-dialog title="编辑货品信息" v-model="editSkuInfo.dialogVisible" width="40%">
      <span>商品货号：</span>
      <span>{{ editSkuInfo.productSn }}</span>
      <el-input placeholder="按sku编号搜索" v-model="editSkuInfo.keyword" style="width: 60%;margin-left: 20px">
        <template #append>
          <el-button :icon="Search" @click="handleSearchEditSku"></el-button>
        </template>
      </el-input>
      <el-table style="width: 100%;margin-top: 20px" :data="editSkuInfo.stockList" border>
        <el-table-column label="SKU编号" align="center">
          <template #default="scope">
            <el-input v-model="scope.row.skuCode"></el-input>
          </template>
        </el-table-column>
        <el-table-column v-for="(item, index) in editSkuInfo.productAttr" :label="item.name" :key="item.id"
          align="center">
          <template #default="scope">
            {{ getProductSkuSp(scope.row, index) }}
          </template>
        </el-table-column>
        <el-table-column label="销售价格" width="80" align="center">
          <template #default="scope">
            <el-input v-model="scope.row.price"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="商品库存" width="80" align="center">
          <template #default="scope">
            <el-input v-model="scope.row.stock"></el-input>
          </template>
        </el-table-column>
        <el-table-column label="库存预警值" width="100" align="center">
          <template #default="scope">
            <el-input v-model="scope.row.lowStock"></el-input>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editSkuInfo.dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleEditSkuConfirm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 审核详情弹框 -->
    <el-dialog title="审核详情" v-model="verifyDialogVisible" width="50%">
      <el-table :data="verifyRecordList" border style="width: 100%">
        <el-table-column prop="createTime" label="审核时间" width="180">
          <template #default="scope">
            {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="vertifyMan" label="审核人" width="120" />
        <el-table-column prop="status" label="审核状态" width="120">
          <template #default="scope">
            {{ scope.row.status === 1 ? '审核通过' : (scope.row.status === 2 ? '审核不通过' : '未知') }}
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="审核详情" />
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 操作日志弹框 -->
    <el-dialog title="操作日志" v-model="logDialogVisible" width="50%">
      <el-table :data="logList" border style="width: 100%">
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="content" label="操作内容" />
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="logDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 审核操作弹框 -->
    <el-dialog :title="verifyOperateData.verifyStatus === 1 ? '审核通过' : '审核不通过'" v-model="verifyOperateDialogVisible" width="40%">
      <el-form label-width="100px">
        <el-form-item label="审核详情：" required>
          <el-input 
            v-model="verifyOperateData.detail" 
            type="textarea" 
            :rows="4"
            placeholder="请输入审核详情（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyOperateDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleVerifyConfirm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style></style>
