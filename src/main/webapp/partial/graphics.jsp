<%@ page contentType="text/html;charset=UTF-8"%>
<div class="graphics">
        <svg width="550" height="550" class="svg-graph" xmlns="http://www.w3.org/2000/svg" >
            <line  x1="0" x2="550" y1="275" y2="275" stroke="black"></line>
            <line x1="275" x2="275" y1="0" y2="550" stroke="black"></line>
            <polygon points="275,0 269,20 281,20" stroke="black"></polygon>
            <polygon points="550,275 535,281 535,269" stroke="black"></polygon>
            <line x1="366" x2="366" y1="260" y2="290" stroke="black"></line>
            <line x1="458" x2="458" y1="260" y2="290" stroke="black"></line>
            <line  x1="183"  x2="183"  y1="260" y2="290" stroke="black"></line>
            <line  x1="91" x2="91" y1="260" y2="290" stroke="black"></line>
            <line x1="260" x2="290" y1="366" y2="366" stroke="black"></line>
            <line  x1="260" x2="290" y1="458"  y2="458"  stroke="black"></line>
            <line x1="260" x2="290" y1="183" y2="183" stroke="black"></line>
            <line  x1="260" x2="290" y1="91" y2="91" stroke="black"></line>
            <text x="345" y="250" stroke="red">R/2</text>
            <text  x="452" y="250" stroke="red">R</text>
            <text  x="70" y="250" stroke="red">-R</text>
            <text  x="150" y="250" stroke="red">-R/2</text>
            <text  x="300" y="190" stroke="red">R/2</text>
            <text  x="300" y="100" stroke="red">R</text>
            <text  x="300" y="375" stroke="red">-R/2</text>
            <text  x="300" y="470" stroke="red">-R</text>

            <polygon  points="91,274 91,183.5 274,183.5, 274,274"
                      fill="blue" fill-opacity="0.3" stroke="blue"></polygon>

            <path   d="M 368,275 L 275,275 L 275,182 A 100,100 0 0 1 366,275"
                    fill="#FF008B" fill-opacity="0.3" stroke="#FF008B"></path>


            <polygon  points="458,275 275,458 275,275"
                      fill="green" fill-opacity="0.3" stroke="#310062"></polygon>

            <circle r="5" cx="275" cy="275" id="target-dot" fill="red"></circle>
        </svg>

</div>

