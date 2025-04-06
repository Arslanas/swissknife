process.env
var details = JSON.parse(process.env.CREATE_MR_DETAILS)

console.log(`Create MR for details 
    branch : ${details.targetBranch} 
    title : ${details.title}`)