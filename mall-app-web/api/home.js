import request from '@/utils/requestUtil'

export function fetchContent() {
	return request({
		method: 'GET',
		url: '/home/content'
	})
}

export function fetchRecommendProductList(params) {
	return request({
		method: 'GET',
		url: '/home/recommendProductList',
		params:params
	})
}

export function fetchProductCateList(parentId) {
	return request({
		method: 'GET',
		url: '/home/productCateList/'+parentId,
	})
}

export function fetchNewProductList(params) {
	return request({
		method: 'GET',
		url: '/home/newProductList',
		params:params
	})
}

export function fetchHotProductList(params) {
	return request({
		method: 'GET',
		url: '/home/hotProductList',
		params:params
	})
}

export function fetchTopicDetail(id) {
	return request({
		method: 'GET',
		url: '/home/topic/' + id
	})
}

export function fetchTopicList(params) {
	return request({
		method: 'GET',
		url: '/home/topicList',
		params: params
	})
}

export function fetchPrefrenceAreaList() {
	return request({
		method: 'GET',
		url: '/home/prefrenceAreaList'
	})
}

export function fetchSubjectList(params) {
	return request({
		method: 'GET',
		url: '/home/subjectList',
		params: params
	})
}

export function fetchSubjectDetail(id) {
	return request({
		method: 'GET',
		url: '/home/subject/' + id
	})
}
