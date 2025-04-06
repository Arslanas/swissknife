var details = {
    title : 'Some title',
    targetBranch : 'origin/release/capg.2025.5.0'
}

if (1 === 1) details = null

console.log(`Create MR for details 
    branch : ${details.targetBranch} 
    title : ${details.title}`)