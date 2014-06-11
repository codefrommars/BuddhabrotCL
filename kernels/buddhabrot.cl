kernel void accum(global const float* x_points, global const float* y_points, int const bailout, global int* out,  int const points) {
	//Get the current itemId
	const int itemId = get_global_id(0); 
	//Out of range?
	if(itemId >= points) {
		return;
    }
	
	//Iterate the point
	float x = 0;
	float y = 0;
	float nx = 0;
	float ny = 0;
	
	for( int i = 0; i < bailout; i++) {
		
		nx = x * x  - y * y + x_points[itemId];
		ny = 2 * x * y + y_points[itemId];
		
		//It scapes
		if( nx * nx + ny*ny > 10 ){
			out[itemId] = i; 	
			return;
		}
		
		x = nx;
		y = ny;
	}
	
	out[itemId] = 0;
}